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


import java.io.File;
import java.io.FileWriter;
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

        // read from resource file

        // shuffle
        shuffleAINames();
        shuffleHumanNames();
    }

    public static void main(String[] args) {
        try {
            List<String> tempFirstNames = new ArrayList<>();
            List<String> tempLastNames = new ArrayList<>();

            File firstNames = new File(NameList.class.getResource("firstnames.txt").toURI());
            File lastNames = new File(NameList.class.getResource("lastnames.txt").toURI());

            System.out.println("Reading first names...");
            Scanner firstNameScanner = new Scanner(firstNames);
            while (firstNameScanner.hasNextLine()) {
                String line = firstNameScanner.nextLine();
                if (!tempFirstNames.contains(line) && !line.equals("")) {
                    tempFirstNames.add(line);
                }
            }

            System.out.println("Reading last names...");
            Scanner lastNameScanner = new Scanner(lastNames);
            while (lastNameScanner.hasNextLine()) {
                String line = lastNameScanner.nextLine();
                if (!tempLastNames.contains(line) && !line.equals("")) {
                    tempLastNames.add(line);
                }
            }

            firstNameScanner.close();
            lastNameScanner.close();
            System.out.println("Reading completed!");

            System.out.println("Read " + tempFirstNames.size() + " first names and " + tempLastNames.size() + " last names");

            // sort the names
            System.out.println("Sorting...");
            Collections.sort(tempFirstNames);
            Collections.sort(tempLastNames);

            // write them back
            System.out.println("Writing first names...");
            FileWriter firstNameWriter = new FileWriter(firstNames);
            for (String firstName : tempFirstNames) {
                firstNameWriter.write(firstName + "\n");
            }
            firstNameWriter.flush();
            firstNameWriter.close();

            System.out.println("Writing last names...");
            FileWriter lastNameWriter = new FileWriter(lastNames);
            for (String lastName : tempLastNames) {
                lastNameWriter.write(lastName + "\n");
            }
            lastNameWriter.flush();
            lastNameWriter.close();

            System.out.println("Done!");
            System.out.println("Files saved as:");
            System.out.println(firstNames.getAbsolutePath());
            System.out.println(lastNames.getAbsolutePath());
        } catch (java.io.IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static void shuffleAINames() {
        long seed = System.nanoTime();
        Collections.shuffle(firstNames, new Random(seed));
    }

    private static void shuffleHumanNames() {
        long seed = System.nanoTime();
        Collections.shuffle(lastNames, new Random(seed));
    }

    public static String getNextAIName() {
        lastAIIndex++;
        if (lastAIIndex >= firstNames.size()) {
            shuffleAINames();
            lastAIIndex = 0;
        }

        return firstNames.get(lastAIIndex);
    }

    public static int getNumberOfAvailableAINames() {
        return firstNames.size();
    }

    public static String getNextHumanName() {
        lastHumanIndex++;
        if (lastHumanIndex >= lastNames.size()) {
            shuffleHumanNames();
            lastHumanIndex = 0;
        }

        return lastNames.get(lastHumanIndex);
    }

    public static int getNumberOfAvailableHumanNames() {
        return lastNames.size();
    }
}
