package com.github.vatbub.tictactoe.server;

/*-
 * #%L
 * tictactoe-server-updater
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


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import common.Common;
import logging.FOKLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Updates the software on the relay server
 */
@SuppressWarnings("WeakerAccess")
public class UpdaterMain {
    public static void main(String[] args) {
        try {
            Common.setAppName("tictactoeserverupdater");
            if (args.length < 3) {
                printHelpMessage();
            } else {
                FOKLogger.info(UpdaterMain.class.getName(), "Initializing the connection to aws ec2...");
                String awsKey = args[0];
                String awsSecret = args[1];
                String instanceID = args[2];
                AWSCredentials credentials = new BasicAWSCredentials(awsKey, awsSecret);
                Regions awsRegion = Regions.EU_CENTRAL_1; // Frankfurt
                AmazonEC2 client = AmazonEC2ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(awsRegion).build();

                try {
                    FOKLogger.info(UpdaterMain.class.getName(), "Sending the shutdown request to AWS EC2...");
                    List<String> instanceIDCopy = new ArrayList<>(1);
                    instanceIDCopy.add(instanceID);
                    StopInstancesRequest stopInstancesRequest = new StopInstancesRequest(instanceIDCopy);
                    StopInstancesResult stopInstancesResult = client.stopInstances(stopInstancesRequest);
                    for (InstanceStateChange item : stopInstancesResult.getStoppingInstances()) {
                        FOKLogger.info(UpdaterMain.class.getName(), "Stopping instance: " + item.getInstanceId() + ", instance state changed from " + item.getPreviousState() + " to " + item.getCurrentState());

                        FOKLogger.info(UpdaterMain.class.getName(), "Waiting for the instance to shut down...");

                        long lastPrintTime = System.currentTimeMillis();
                        DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
                        List<String> instanceId = new ArrayList<>(1);
                        instanceId.add(instanceID);
                        describeInstancesRequest.setInstanceIds(instanceId);
                        DescribeInstancesResult describeInstancesResult;
                        Instance newInstance = null;
                        int retries = 0;

                        do {
                            // we're waiting

                            if (System.currentTimeMillis() - lastPrintTime >= Math.pow(2, retries) * 100) {
                                retries = retries + 1;
                                describeInstancesResult = client.describeInstances(describeInstancesRequest);
                                newInstance = describeInstancesResult.getReservations().get(0).getInstances().get(0);
                                lastPrintTime = System.currentTimeMillis();
                                if (newInstance.getState().getCode() != 80) {
                                    FOKLogger.info(UpdaterMain.class.getName(), "Still waiting for the instance to shut down, current instance state is " + newInstance.getState().getName());
                                } else {
                                    FOKLogger.info(UpdaterMain.class.getName(), "Instance is " + newInstance.getState().getName());
                                }
                            }
                        } while (newInstance == null || newInstance.getState().getCode() != 80);
                    }
                } catch (AmazonEC2Exception e) {
                    FOKLogger.severe(UpdaterMain.class.getName(), "Could not stop instance " + instanceID + ": " + e.getMessage());
                }

                try {
                    FOKLogger.info(UpdaterMain.class.getName(), "Sending the start request to AWS EC2...");
                    List<String> instanceIDCopy = new ArrayList<>(1);
                    instanceIDCopy.add(instanceID);
                    StartInstancesRequest startInstancesRequest = new StartInstancesRequest(instanceIDCopy);
                    StartInstancesResult startInstancesResult = client.startInstances(startInstancesRequest);
                    for (InstanceStateChange item : startInstancesResult.getStartingInstances()) {
                        FOKLogger.info(UpdaterMain.class.getName(), "Started instance: " + item.getInstanceId() + ", instance state changed from " + item.getPreviousState() + " to " + item.getCurrentState());
                    }
                    FOKLogger.info(UpdaterMain.class.getName(), "The StartInstancesRequest has been sent successfully. Please note though that this program will not wait for the instance to boot.");
                } catch (AmazonEC2Exception e) {
                    FOKLogger.severe(UpdaterMain.class.getName(), "Could not start instance " + instanceID + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            printHelpMessage();
            FOKLogger.log(UpdaterMain.class.getName(), Level.SEVERE, "An error occurred.", e);
        }
    }

    /**
     * Prints the help message on the console
     */
    private static void printHelpMessage() {
        FOKLogger.info(UpdaterMain.class.getName(), "TicTacToe Server Updater version " + Common.getAppVersion());
        FOKLogger.info(UpdaterMain.class.getName(), "Sends a request to the specified server to update itself");
        FOKLogger.info(UpdaterMain.class.getName(), "Required arguments:");
        FOKLogger.info(UpdaterMain.class.getName(), "awsKey: The key to authenticate on aws");
        FOKLogger.info(UpdaterMain.class.getName(), "awsSecret: The secret to authenticate on aws");
        FOKLogger.info(UpdaterMain.class.getName(), "instanceID: The id of the instance to update");
    }
}
