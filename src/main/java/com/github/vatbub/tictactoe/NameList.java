package com.github.vatbub.tictactoe;

/*-
 * #%L
 * tictactoe
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


import org.apache.commons.lang.WordUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * A list of firstNames for ai players
 */
@SuppressWarnings("WeakerAccess")
public class NameList {
    private static final List<String> firstNames;
    private static final List<String> lastNames;
    private static int lastAIIndex = -1;
    private static int lastHumanIndex = -1;

    static {
        firstNames = new ArrayList<>();
        lastNames = new ArrayList<>();

        try {
            // read from resource file
            File firstNamesFile = new File(NameList.class.getResource("firstnames.txt").toURI());
            File lastNamesFile = new File(NameList.class.getResource("lastnames.txt").toURI());

            Scanner firstNameScanner = new Scanner(firstNamesFile);
            while (firstNameScanner.hasNextLine()) {
                String line = firstNameScanner.nextLine();
                if (!firstNames.contains(line) && !line.equals("")) {
                    firstNames.add(line);
                }
            }

            Scanner lastNameScanner;
            lastNameScanner = new Scanner(lastNamesFile);
            while (lastNameScanner.hasNextLine()) {
                String line = lastNameScanner.nextLine();
                if (!lastNames.contains(line) && !line.equals("")) {
                    lastNames.add(line);
                }
            }

            firstNameScanner.close();
            lastNameScanner.close();

            // shuffle
            shuffle();
        } catch (FileNotFoundException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static void shuffle() {
        Collections.shuffle(firstNames, new Random(System.nanoTime()));
        Collections.shuffle(lastNames, new Random(System.nanoTime()));
    }

    public static String getNextFirstName() {
        lastAIIndex++;
        if (lastAIIndex >= firstNames.size()) {
            shuffle();
            lastAIIndex = 0;
        }

        return WordUtils.capitalize(firstNames.get(lastAIIndex));
    }

    public static int getNumberOfAvailableFirstNames() {
        return firstNames.size();
    }

    public static String getNextLastName() {
        lastHumanIndex++;
        if (lastHumanIndex >= lastNames.size()) {
            shuffle();
            lastHumanIndex = 0;
        }

        return WordUtils.capitalize(lastNames.get(lastHumanIndex));
    }

    public static String getNextName(){
        return getNextFirstName() + " " + getNextLastName();
    }

    public static int getNumberOfAvailableLastNames() {
        return lastNames.size();
    }
}
