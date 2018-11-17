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
import com.github.vatbub.tictactoe.common.testing.TomcatTest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jsunsoft.http.HttpRequest;
import com.jsunsoft.http.HttpRequestBuilder;
import com.jsunsoft.http.NoSuchContentException;
import com.jsunsoft.http.ResponseDeserializer;
import org.apache.catalina.LifecycleException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

/**
 * Tests the server
 */
@SuppressWarnings("Duplicates")
public class ServerServletTest extends TomcatTest {
    private static final int TOMCAT_PORT = 9999;
    private static final String apiSuffix = "tictactoe";
    /**
     * The port to be used to test the server
     */
    private static Gson gson;
    private static ServerServlet api;
    private final String identifierPrefix = "testuser";
    private final String identifier1 = identifierPrefix + Math.round(Math.random() * 1000);
    private final String identifier2 = identifierPrefix + Math.round(Math.random() * 1000);
    private OnlineMultiPlayerRequestOpponentRequest request1;
    private OnlineMultiPlayerRequestOpponentRequest request2;
    private Throwable throwable;
    private String connectionId1;
    private String connectionId2;

    @BeforeClass
    public static void doYourOneTimeSetup() throws IOException, LifecycleException {
        api = new ServerServlet();
        TomcatTest.startServer(TOMCAT_PORT, "", "ServerServlet", api, "/" + apiSuffix);
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Before
    public void setUp() throws MalformedURLException, URISyntaxException {
        api.resetServer();

        GetConnectionIdResponse getConnectionIdResponse1 = doRequestWithType(new GetConnectionIdRequest());
        connectionId1 = getConnectionIdResponse1.getConnectionId();

        GetConnectionIdResponse getConnectionIdResponse2 = doRequestWithType(new GetConnectionIdRequest());
        connectionId2 = getConnectionIdResponse2.getConnectionId();

        // set up the requests
        request1 = new OnlineMultiPlayerRequestOpponentRequest(connectionId1);
        request2 = new OnlineMultiPlayerRequestOpponentRequest(connectionId2);
        request1.setClientIdentifier(identifier1);
        request2.setClientIdentifier(identifier2);
    }

    private String doRequest(String json) throws MalformedURLException, URISyntaxException {
        FOKLogger.info(getClass().getName(), "Sending the following json:\n" + json);
        HttpRequest<String> httpRequest = HttpRequestBuilder.createPost(new URL(new URL("http", "localhost", TOMCAT_PORT, ""), apiSuffix).toURI(), String.class)
                .responseDeserializer(ResponseDeserializer.ignorableDeserializer()).build();
        String responseJson = httpRequest.executeWithBody(json).get();
        FOKLogger.info(getClass().getName(), "Received the following json:\n" + responseJson);
        return responseJson;
    }

    private <T extends ServerInteraction> T doRequestWithType(ServerInteraction request) throws MalformedURLException, URISyntaxException {
        ServerInteraction response = doRequest(request);
        //noinspection unchecked
        return (T) response;
    }

    private ServerInteraction doRequest(ServerInteraction request) throws MalformedURLException, URISyntaxException {
        String json = doRequest(gson.toJson(request));
        ServerInteraction response = gson.fromJson(json, ServerInteractionImpl.class);
        switch (response.getClassName()) {
            case ServerServlet.COMMON_PACKAGE_NAME + ".CancelGameResponse":
                return gson.fromJson(json, CancelGameResponse.class);
            case ServerServlet.COMMON_PACKAGE_NAME + ".BadRequestException":
                return gson.fromJson(json, BadRequestException.class);
            case ServerServlet.COMMON_PACKAGE_NAME + ".GetConnectionIdResponse":
                return gson.fromJson(json, GetConnectionIdResponse.class);
            case ServerServlet.COMMON_PACKAGE_NAME + ".GetGameDataResponse":
                return gson.fromJson(json, GetGameDataResponse.class);
            case ServerServlet.COMMON_PACKAGE_NAME + ".OnlineMultiPlayerRequestOpponentException":
                return gson.fromJson(json, OnlineMultiPlayerRequestOpponentException.class);
            case ServerServlet.COMMON_PACKAGE_NAME + ".OnlineMultiPlayerRequestOpponentResponse":
                return gson.fromJson(json, OnlineMultiPlayerRequestOpponentResponse.class);
            case ServerServlet.COMMON_PACKAGE_NAME + ".RemoveDataResponse":
                return gson.fromJson(json, RemoveDataResponse.class);
            case ServerServlet.COMMON_PACKAGE_NAME + ".MoveResponse":
                return gson.fromJson(json, MoveResponse.class);
            default:
                return response;
        }
    }

    @Test
    public void singleRequestNoDesiredOpponent() throws Throwable {
        ServerInteraction object = doRequest(request1);
        assertWaitForOpponent(object);
    }

    private void assertWaitForOpponent(Object object) {
        assert object instanceof OnlineMultiPlayerRequestOpponentResponse;
        OnlineMultiPlayerRequestOpponentResponse response = (OnlineMultiPlayerRequestOpponentResponse) object;
        Assert.assertEquals(ResponseCode.WaitForOpponent, response.getResponseCode());
    }

    private void assertOpponentFound(Object object, String expectedOpponent) {
        assert object instanceof OnlineMultiPlayerRequestOpponentResponse;
        OnlineMultiPlayerRequestOpponentResponse response = (OnlineMultiPlayerRequestOpponentResponse) object;
        Assert.assertEquals(ResponseCode.OpponentFound, response.getResponseCode());
        Assert.assertEquals(expectedOpponent, response.getOpponentIdentifier());
    }

    @Test
    public void twoRequestsNoDesiredOpponent() throws Throwable {
        ServerInteraction response1 = doRequest(request1);
        assertWaitForOpponent(response1);

        ServerInteraction response2 = doRequest(request2);
        assertOpponentFound(response2, request1.getClientIdentifier());
        Assert.assertFalse(((OnlineMultiPlayerRequestOpponentResponse) response2).hasFirstTurn());

        ServerInteraction response3 = doRequest(request1);
        assertOpponentFound(response3, request2.getClientIdentifier());
        Assert.assertTrue(((OnlineMultiPlayerRequestOpponentResponse) response3).hasFirstTurn());
    }

    @Test
    public void singleRequestWithDesiredOpponent() throws Throwable {
        request1.setDesiredOpponentIdentifier(identifier2);
        ServerInteraction response = doRequest(request1);
        assertWaitForOpponent(response);
    }

    @Test
    public void multipleRequestsFromSameClient() throws Throwable {
        request1.setDesiredOpponentIdentifier(identifier2);
        ServerInteraction response1 = doRequest(request1);
        assertWaitForOpponent(response1);

        ServerInteraction response2 = doRequest(request1.clone());
        assertWaitForOpponent(response2);

        ServerInteraction response3 = doRequest(request1.clone());
        assertWaitForOpponent(response3);
    }

    @Test
    public void twoRequestsWithDesiredOpponent() throws Throwable {
        request1.setDesiredOpponentIdentifier(identifier2);
        ServerInteraction response1 = doRequest(request1);
        assertWaitForOpponent(response1);

        request2.setDesiredOpponentIdentifier(identifier1);
        ServerInteraction response2 = doRequest(request2);
        assertOpponentFound(response2, request1.getClientIdentifier());
        Assert.assertFalse(((OnlineMultiPlayerRequestOpponentResponse) response2).hasFirstTurn());

        ServerInteraction response3 = doRequest(request1);
        assertOpponentFound(response3, request2.getClientIdentifier());
        Assert.assertTrue(((OnlineMultiPlayerRequestOpponentResponse) response3).hasFirstTurn());
    }

    @Test
    public void sendSameRequestTwice() throws Throwable {
        ServerInteraction response1 = doRequest(request1);
        assertWaitForOpponent(response1);

        ServerInteraction response2 = doRequest(request1);
        assertWaitForOpponent(response2);
    }

    @Test
    public void abortedRequestTest() throws Throwable {
        ServerInteraction response1 = doRequest(request1);
        assertWaitForOpponent(response1);

        request1.setOperation(Operation.AbortRequest);
        ServerInteraction response2 = doRequest(request1);
        Assert.assertTrue(response2 instanceof OnlineMultiPlayerRequestOpponentResponse);
        OnlineMultiPlayerRequestOpponentResponse response2Cast = (OnlineMultiPlayerRequestOpponentResponse) response2;
        Assert.assertEquals(ResponseCode.RequestAborted, response2Cast.getResponseCode());
    }

    @Test
    public void abortRequestThatWasNeverSentTest() throws Throwable {
        request1.setOperation(Operation.AbortRequest);
        try {
            doRequest(request1);
            Assert.fail("NoSuchContentException expected");
        } catch (NoSuchContentException e) {
            FOKLogger.log(ServerServletTest.class.getName(), Level.INFO, "Expected exception was thrown", e);
        }
    }

    @Test
    public void unknownConnectionIdTest() throws Throwable {
        // connection id is a hex value, 'zzz' is therefore illegal
        OnlineMultiPlayerRequestOpponentRequest request = new OnlineMultiPlayerRequestOpponentRequest("zzz");
        try {
            doRequest(request);
            Assert.fail("NoSuchContentException expected");
        } catch (NoSuchContentException e) {
            FOKLogger.log(ServerServletTest.class.getName(), Level.INFO, "Expected exception was thrown", e);
        }
    }

    @Test
    public void unparsableClassTest() throws Throwable {
        UnparsableRequest request = new UnparsableRequest(connectionId1);
        try {
            doRequest(request);
            Assert.fail("NoSuchContentException expected");
        } catch (NoSuchContentException e) {
            FOKLogger.log(ServerServletTest.class.getName(), Level.INFO, "Expected exception was thrown", e);
        }
    }

    @Test
    public void getConnectionIdWithSpecifiedConnectionId() throws MalformedURLException, URISyntaxException {
        GetConnectionIdRequest request = new GetConnectionIdRequest();
        request.setConnectionId(connectionId1);
        try {
            doRequest(request);
            Assert.fail("NoSuchContentException expected");
        } catch (NoSuchContentException e) {
            FOKLogger.log(ServerServletTest.class.getName(), Level.INFO, "Expected exception was thrown", e);
        }
    }

    @Test
    public void onlineMultiPlayerRequestOpponentRequestWithNoConnectionId() throws MalformedURLException, URISyntaxException {
        OnlineMultiPlayerRequestOpponentRequest request = new OnlineMultiPlayerRequestOpponentRequest(null);
        try {
            doRequest(request);
            Assert.fail("NoSuchContentException expected");
        } catch (NoSuchContentException e) {
            FOKLogger.log(ServerServletTest.class.getName(), Level.INFO, "Expected exception was thrown", e);
        }
    }

    @Test
    public void moveRequestWithNoConnectionId() throws MalformedURLException, URISyntaxException {
        MoveRequest request = new MoveRequest(null, new Move(1, 1));
        try {
            doRequest(request);
            Assert.fail("NoSuchContentException expected");
        } catch (NoSuchContentException e) {
            FOKLogger.log(ServerServletTest.class.getName(), Level.INFO, "Expected exception was thrown", e);
        }
    }

    @Test
    public void getGameDataRequestWithNoConnectionId() throws MalformedURLException, URISyntaxException {
        GetGameDataRequest request = new GetGameDataRequest(null);
        try {
            doRequest(request);
            Assert.fail("NoSuchContentException expected");
        } catch (NoSuchContentException e) {
            FOKLogger.log(ServerServletTest.class.getName(), Level.INFO, "Expected exception was thrown", e);
        }
    }

    @Test
    public void removeDataRequestWithNoConnectionId() throws MalformedURLException, URISyntaxException {
        RemoveDataRequest request = new RemoveDataRequest(null);
        try {
            doRequest(request);
            Assert.fail("NoSuchContentException expected");
        } catch (NoSuchContentException e) {
            FOKLogger.log(ServerServletTest.class.getName(), Level.INFO, "Expected exception was thrown", e);
        }
    }

    @Test
    public void cancelGameRequestWithNoConnectionId() throws MalformedURLException, URISyntaxException {
        CancelGameRequest request = new CancelGameRequest(null);
        try {
            doRequest(request);
            Assert.fail("NoSuchContentException expected");
        } catch (NoSuchContentException e) {
            FOKLogger.log(ServerServletTest.class.getName(), Level.INFO, "Expected exception was thrown", e);
        }
    }

    @Test
    public void moveRequestWhileNotEnrolledInGameId() throws MalformedURLException, URISyntaxException {
        MoveRequest request = new MoveRequest(connectionId1, new Move(1, 1));
        try {
            doRequest(request);
            Assert.fail("NoSuchContentException expected");
        } catch (NoSuchContentException e) {
            FOKLogger.log(ServerServletTest.class.getName(), Level.INFO, "Expected exception was thrown", e);
        }
    }

    @Test
    public void moveTest() throws Throwable {
        twoRequestsNoDesiredOpponent();
        MoveRequest moveRequest1 = new MoveRequest(connectionId1, new Move(1, 1));
        MoveRequest moveRequest2 = new MoveRequest(connectionId2, new Move(2, 2));

        ServerInteraction response1 = doRequest(moveRequest1);
        Assert.assertTrue(response1 instanceof MoveResponse);

        ServerInteraction response2 = doRequest(new GetGameDataRequest(connectionId2));
        Assert.assertTrue(response2 instanceof GetGameDataResponse);
        GetGameDataResponse gameDataResponse1 = (GetGameDataResponse) response2;
        Assert.assertFalse(gameDataResponse1.isGameCancelled());
        Assert.assertEquals(1, gameDataResponse1.getMoves().size());
        Assert.assertEquals(moveRequest1.getMove(), gameDataResponse1.getMoves().get(0));

        ServerInteraction response3 = doRequest(moveRequest2);
        Assert.assertTrue(response3 instanceof MoveResponse);

        ServerInteraction response4 = doRequest(new GetGameDataRequest(connectionId1));
        Assert.assertTrue(response4 instanceof GetGameDataResponse);
        GetGameDataResponse gameDataResponse2 = (GetGameDataResponse) response4;
        Assert.assertFalse(gameDataResponse2.isGameCancelled());
        Assert.assertEquals(1, gameDataResponse2.getMoves().size());
        Assert.assertEquals(moveRequest2.getMove(), gameDataResponse2.getMoves().get(0));
    }

    @Test
    public void cancelGameTest() throws Throwable {
        twoRequestsNoDesiredOpponent();

        ServerInteraction response1 = doRequest(new GetGameDataRequest(connectionId1));
        Assert.assertTrue(response1 instanceof GetGameDataResponse);
        GetGameDataResponse gameDataResponse1 = (GetGameDataResponse) response1;
        Assert.assertFalse(gameDataResponse1.isGameCancelled());

        ServerInteraction response2 = doRequest(new GetGameDataRequest(connectionId2));
        Assert.assertTrue(response2 instanceof GetGameDataResponse);
        GetGameDataResponse gameDataResponse2 = (GetGameDataResponse) response2;
        Assert.assertFalse(gameDataResponse2.isGameCancelled());

        CancelGameRequest cancelGameRequest = new CancelGameRequest(connectionId1, "Reason1");
        ServerInteraction cancelGameResponse = doRequest(cancelGameRequest);
        Assert.assertTrue(cancelGameResponse instanceof CancelGameResponse);
        CancelGameResponse cancelGameResponseCast = (CancelGameResponse) cancelGameResponse;
        Assert.assertEquals(cancelGameRequest.getReason(), cancelGameResponseCast.getReason());

        ServerInteraction response3 = doRequest(new GetGameDataRequest(connectionId1));
        Assert.assertTrue(response3 instanceof GetGameDataResponse);
        GetGameDataResponse gameDataResponse3 = (GetGameDataResponse) response3;
        Assert.assertTrue(gameDataResponse3.isGameCancelled());
        Assert.assertEquals(cancelGameRequest.getReason(), gameDataResponse3.getCancelReason());

        ServerInteraction response4 = doRequest(new GetGameDataRequest(connectionId2));
        Assert.assertTrue(response4 instanceof GetGameDataResponse);
        GetGameDataResponse gameDataResponse4 = (GetGameDataResponse) response4;
        Assert.assertTrue(gameDataResponse4.isGameCancelled());
        Assert.assertEquals(cancelGameRequest.getReason(), gameDataResponse4.getCancelReason());
    }

    @Test
    public void cancelGameRequestWhileNotEnrolledInGameId() throws MalformedURLException, URISyntaxException {
        CancelGameRequest request = new CancelGameRequest(connectionId1);
        try {
            doRequest(request);
            Assert.fail("NoSuchContentException expected");
        } catch (NoSuchContentException e) {
            FOKLogger.log(ServerServletTest.class.getName(), Level.INFO, "Expected exception was thrown", e);
        }
    }

    @Test
    public void removeDataWithoutCancellationTest() throws Throwable {
        removeDataTestImpl(false);
    }

    @Test
    public void removeDataTest() throws Throwable {
        removeDataTestImpl(true);
    }

    private void removeDataTestImpl(boolean cancelGames) throws Throwable {
        twoRequestsNoDesiredOpponent();

        RemoveDataRequest request1 = new RemoveDataRequest(connectionId1);
        request1.setCancelGames(cancelGames);
        ServerInteraction response1 = doRequest(request1);
        Assert.assertTrue(response1 instanceof RemoveDataResponse);

        ServerInteraction response2 = doRequest(new GetGameDataRequest(connectionId2));
        Assert.assertTrue(response2 instanceof GetGameDataResponse);
        GetGameDataResponse gameDataResponse2 = (GetGameDataResponse) response2;
        Assert.assertEquals(cancelGames, gameDataResponse2.isGameCancelled());
        if (cancelGames)
            Assert.assertEquals("The opponent disconnected from the server.", gameDataResponse2.getCancelReason());
        else
            Assert.assertNull(gameDataResponse2.getCancelReason());

        // Should error as connection id should be unknown
        CancelGameRequest request = new CancelGameRequest(connectionId1);
        try {
            doRequest(request);
            Assert.fail("NoSuchContentException expected");
        } catch (NoSuchContentException e) {
            FOKLogger.log(ServerServletTest.class.getName(), Level.INFO, "Expected exception was thrown", e);
        }
    }

    public class UnparsableRequest extends Request {

        public UnparsableRequest(String connectionId) {
            super(connectionId, UnparsableRequest.class.getCanonicalName());
        }
    }
}
