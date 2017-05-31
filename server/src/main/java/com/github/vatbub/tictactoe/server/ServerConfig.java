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


import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Frederik on 31/05/2017.
 */
public class ServerConfig {
    public static URL updateMavenRepoURL;
    public static URL updateMavenSnapshotRepoURL;

    static {
        try {
            updateMavenRepoURL = new URL("https://dl.bintray.com/vatbub/fokprojectsReleases");
            updateMavenSnapshotRepoURL=new URL("https://oss.jfrog.org/artifactory/repo");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static final String artifactID = "tictactoe.server";
    public static final String groupID = "com.github.vatbub";
    public static final String classifier = "jar-with-dependencies";
}
