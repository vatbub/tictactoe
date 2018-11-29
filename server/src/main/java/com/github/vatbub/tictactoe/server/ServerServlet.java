package com.github.vatbub.tictactoe.server;

/*-
 * #%L
 * tictactoe-server
 * %%
 * Copyright (C) 2016 - 2017 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.github.vatbub.common.core.logging.FOKLogger;
import com.github.vatbub.tictactoe.common.*;
import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Launches the server
 */
@SuppressWarnings("WeakerAccess")
public class ServerServlet extends HttpServlet {
    public static final String COMMON_PACKAGE_NAME = "com.github.vatbub.tictactoe.common";
    private final Map<String, GetGameDataResponse> gameData = new HashMap<>();
    private final Map<String, OnlineMultiPlayerRequestOpponentRequest> pendingMatches = new HashMap<>();
    private final Map<String, String> connectionMap = new HashMap<>();
    private final List<String> connectionIdsInUse = new LinkedList<>();
    private final Random random = new Random();
    private final Map<String, List<OnlineMultiPlayerRequestOpponentRequest>> openRequests = new HashMap<>();
    private Gson gson;
    private final String encoding = "UTF-8";

    private Gson getGson() {
        if (gson == null)
            gson = new Gson();
        return gson;
    }

    /**
     * Clears the internal server memory (like a restart) but without a actual restart
     */
    public void resetServer() {
        gameData.clear();
        pendingMatches.clear();
        connectionMap.clear();
        connectionIdsInUse.clear();
        openRequests.clear();
    }

    private String generateAndStoreNewConnectionId() {
        String connectionString;

        do {
            int connectionId = random.nextInt();
            if (connectionId < 0)
                connectionId = -connectionId;

            connectionString = Integer.toString(connectionId, 16);
        } while (connectionIdsInUse.contains(connectionString));

        connectionIdsInUse.add(connectionString);
        return connectionString;
    }

    private GetGameDataResponse getOrCreateGameData(String connection) {
        if (!gameData.containsKey(connection))
            gameData.put(connection, new GetGameDataResponse(connection, new ArrayList<>(), false));

        return gameData.get(connection);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuilder requestBodyBuilder = new StringBuilder();
        req.getReader().lines().forEachOrdered(requestBodyBuilder::append);
        String requestBody = requestBodyBuilder.toString();

        ServerInteraction serverInteraction = getGson().fromJson(requestBody, ServerInteractionImpl.class);
        FOKLogger.info(getClass().getName(), "Received object, getClassName() = " + serverInteraction.getClassName());

        String connection = null;
        if (serverInteraction.getConnectionId() != null) connection = serverInteraction.getConnectionId();

        Response response = null;
        ServerInteractionException exception = null;
        int statusCode = -1;

        try {
            if (connection != null && !connectionIdsInUse.contains(connection))
                throw new IllegalArgumentException("ConnectionId unknown");

            switch (serverInteraction.getClassName()) {
                case COMMON_PACKAGE_NAME + ".RemoveDataRequest":
                    if (connection == null)
                        throw new IllegalArgumentException("The connection id must not be null");

                    FOKLogger.info(getClass().getName(), "Removing all data from the client that requested all data to be deleted...");
                    RemoveDataRequest removeDataRequest = getGson().fromJson(requestBody, RemoveDataRequest.class);

                    if (connectionMap.containsKey(connection)) {
                        // send the opponent the message that the game has been disconnected
                        String matchingConnection = connectionMap.get(connection);

                        if (removeDataRequest.isCancelGames()) {
                            getOrCreateGameData(matchingConnection).setGameCancelled(true);
                            getOrCreateGameData(matchingConnection).setCancelReason("The opponent disconnected from the server.");
                        }

                        connectionMap.remove(connection);
                        connectionMap.remove(matchingConnection);
                    }

                    openRequests.remove(connection);
                    gameData.remove(connection);
                    pendingMatches.remove(connection);
                    connectionIdsInUse.remove(connection);

                    response = new RemoveDataResponse(connection);
                    break;
                case COMMON_PACKAGE_NAME + ".CancelGameRequest":
                    if (connection == null)
                        throw new IllegalArgumentException("The connection id must not be null");
                    if (!connectionMap.containsKey(connection))
                        throw new IllegalArgumentException("The specified connection is currently not enrolled in a game");

                    CancelGameRequest cancelGameRequest = getGson().fromJson(requestBody, CancelGameRequest.class);

                    getOrCreateGameData(connection).setGameCancelled(true);
                    getOrCreateGameData(connectionMap.get(connection)).setGameCancelled(true);
                    getOrCreateGameData(connection).setCancelReason(cancelGameRequest.getReason());
                    getOrCreateGameData(connectionMap.get(connection)).setCancelReason(cancelGameRequest.getReason());
                    response = new CancelGameResponse(connection, cancelGameRequest.getReason());
                    break;
                case COMMON_PACKAGE_NAME + ".GetConnectionIdRequest":
                    if (connection != null)
                        throw new IllegalArgumentException("ConnectionId must me null for a GetConnectionIdRequest");

                    response = new GetConnectionIdResponse(generateAndStoreNewConnectionId());
                    break;
                case COMMON_PACKAGE_NAME + ".MoveRequest":
                    if (connection == null)
                        throw new IllegalArgumentException("The connection id must not be null");
                    if (!connectionMap.containsKey(connection))
                        throw new IllegalArgumentException("The specified connection is currently not enrolled in a game");
                    MoveRequest receivedMove = getGson().fromJson(requestBody, MoveRequest.class);
                    getOrCreateGameData(connectionMap.get(connection)).getMoves().add(receivedMove.getMove());
                    response = new MoveResponse(connection);
                    break;
                case COMMON_PACKAGE_NAME + ".GetGameDataRequest":
                    if (connection == null)
                        throw new IllegalArgumentException("The connection id must not be null");

                    response = getOrCreateGameData(connection);
                    break;
                case COMMON_PACKAGE_NAME + ".IsEnrolledInGameRequest":
                    if (connection == null)
                        throw new IllegalArgumentException("The connection id must not be null");
                    response = new IsEnrolledInGameResponse(connection, connectionMap.containsKey(connection));
                    break;
                case COMMON_PACKAGE_NAME + ".OnlineMultiPlayerRequestOpponentRequest":
                    if (connection == null)
                        throw new IllegalArgumentException("The connection id must not be null");

                    OnlineMultiPlayerRequestOpponentRequest receivedRequest = getGson().fromJson(requestBody, OnlineMultiPlayerRequestOpponentRequest.class);

                    FOKLogger.info(ServerServlet.class.getName(), "Received OnlineMultiPlayerRequestOpponentRequest");
                    FOKLogger.info(ServerServlet.class.getName(), "Requested operation: " + receivedRequest.getOperation());

                    // Check whether we have a match already
                    if (pendingMatches.containsKey(connection)) {
                        FOKLogger.info(getClass().getName(), "Request has a pending match");
                        response = new OnlineMultiPlayerRequestOpponentResponse(connection, ResponseCode.OpponentFound, pendingMatches.get(connection).getClientIdentifier());
                        ((OnlineMultiPlayerRequestOpponentResponse) response).setHasFirstTurn(true);
                        pendingMatches.remove(connection);
                        break;
                    }

                    if (receivedRequest.getDesiredOpponentIdentifier() == null) {
                        FOKLogger.info(ServerServlet.class.getName(), "The request contains no desired opponent");
                    } else {
                        FOKLogger.info(ServerServlet.class.getName(), "The request contains a desired opponent");
                    }

                    FOKLogger.info(ServerServlet.class.getName(), "Checking for matching requests...");
                    if (receivedRequest.getOperation().equals(Operation.RequestOpponent)) {
                        // check if any of the open requests has a matching desiredOpponentIdentifier
                        String matchingConnection = null;
                        String matchingOpponentIdentifier = null;
                        for (Map.Entry<String, List<OnlineMultiPlayerRequestOpponentRequest>> entry : openRequests.entrySet()) {
                            for (OnlineMultiPlayerRequestOpponentRequest comparedRequest : entry.getValue()) {
                                if (connection.equals(comparedRequest.getConnectionId()))
                                    continue;
                                if (receivedRequest.getDesiredOpponentIdentifier() == null && comparedRequest.getDesiredOpponentIdentifier() == null) {
                                    // found two requests that both don't wish a particular opponent
                                    FOKLogger.info(ServerServlet.class.getName(), "Found matching request!");
                                    matchingConnection = entry.getKey();
                                    matchingOpponentIdentifier = comparedRequest.getClientIdentifier();
                                    break;
                                } else if (comparedRequest.getDesiredOpponentIdentifier() != null && receivedRequest.getClientIdentifier().equals(comparedRequest.getDesiredOpponentIdentifier())) {
                                    FOKLogger.info(ServerServlet.class.getName(), "Found matching request!");
                                    matchingConnection = entry.getKey();
                                    matchingOpponentIdentifier = comparedRequest.getClientIdentifier();
                                    break;
                                }
                            }
                        }

                        if (matchingConnection != null) {
                            openRequests.remove(matchingConnection);
                            // send a response to the client that just requested an opponent
                            response = new OnlineMultiPlayerRequestOpponentResponse(connection, ResponseCode.OpponentFound, matchingOpponentIdentifier);
                            connectionMap.put(connection, matchingConnection);
                            connectionMap.put(matchingConnection, connection);

                            // send request to the opponent too
                            pendingMatches.put(matchingConnection, receivedRequest);
                        } else {
                            // add the request to the openRequests-map
                            FOKLogger.info(ServerServlet.class.getName(), "No matching request found");
                            response = new OnlineMultiPlayerRequestOpponentResponse(connection, ResponseCode.WaitForOpponent);

                            if (!openRequests.containsKey(connection)) {
                                // First request through this connection
                                List<OnlineMultiPlayerRequestOpponentRequest> tempRequestList = new ArrayList<>();
                                tempRequestList.add(receivedRequest);
                                openRequests.put(connection, tempRequestList);
                            } else {
                                // client has already sent a request
                                List<OnlineMultiPlayerRequestOpponentRequest> clientRequestList = openRequests.get(connection);
                                if (!clientRequestList.contains(receivedRequest)) {
                                    // request was only sent once so add it to the list
                                    clientRequestList.add(receivedRequest);
                                }
                            }
                        }
                    } else {
                        // operation is AbortRequest
                        List<OnlineMultiPlayerRequestOpponentRequest> clientRequestList = openRequests.get(connection);
                        if (clientRequestList != null && clientRequestList.contains(receivedRequest)) {
                            clientRequestList.remove(receivedRequest);
                            if (clientRequestList.size() == 0) {
                                // delete the inet address from the map
                                FOKLogger.info(ServerServlet.class.getName(), "Matching request found, aborting request...");
                                openRequests.remove(connection);
                            }
                            response = new OnlineMultiPlayerRequestOpponentResponse(connection, ResponseCode.RequestAborted);
                        } else {
                            FOKLogger.severe(ServerServlet.class.getName(), "Could not abort request, no matching request found!");
                            exception = new OnlineMultiPlayerRequestOpponentException("No matching request found.");
                            statusCode = HttpServletResponse.SC_NOT_FOUND;
                            break;
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unable to parse request of class: " + serverInteraction.getClassName());
            }

            if (exception == null && response == null) {
                throw new IllegalStateException("No response generated by server");
            }

        } catch (IllegalArgumentException e) {
            statusCode = HttpServletResponse.SC_BAD_REQUEST;
            exception = new BadRequestException(e.getClass().getName() + ", " + e.getMessage());
        } catch (Exception e) {
            FOKLogger.log(ServerServlet.class.getName(), Level.SEVERE, "A internal server error occurred", e);
            statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            exception = new OnlineMultiPlayerRequestOpponentException(e.getClass().getName() + ", " + e.getMessage());
        }

        String responseJson;

        if (exception != null) {
            FOKLogger.info(ServerServlet.class.getName(), "Sending exception to client...");
            responseJson = getGson().toJson(exception);
        } else {
            FOKLogger.info(ServerServlet.class.getName(), "Sending response to client...");
            statusCode = HttpServletResponse.SC_OK;
            responseJson = getGson().toJson(response);
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding(encoding);
        resp.setStatus(statusCode);

        byte[] responseBytes = responseJson.getBytes(encoding);
        resp.getOutputStream().write(responseBytes);

        resp.getOutputStream().flush();
        resp.getOutputStream().close();
    }
}
