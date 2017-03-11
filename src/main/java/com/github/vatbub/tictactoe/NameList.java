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


import java.util.*;

/**
 * A list of aiNames for ai players
 */
@SuppressWarnings("WeakerAccess")
public class NameList {
    private static List<String> aiNames;
    private static List<String> humanNames;
    private static int lastAIIndex = -1;
    private static int lastHumanIndex = -1;

    static {
        // ai names
        aiNames = new ArrayList<>();
        aiNames.add("The Engine");
        aiNames.add("Joe");
        aiNames.add("MARAX");
        aiNames.add("EPICAC");
        aiNames.add("The Prime Radiant");
        aiNames.add("Mark V");
        aiNames.add("Karl");
        aiNames.add("Mima");
        aiNames.add("Bossy");
        aiNames.add("The City Fathers");
        aiNames.add("Multivac");
        aiNames.add("The Central Computer");
        aiNames.add("Miniac");
        aiNames.add("Cosmic AC");
        aiNames.add("Vulcan 2");
        aiNames.add("Mycroft Holmes");
        aiNames.add("The Ox");
        aiNames.add("The Brain");
        aiNames.add("T.E.N.C.H. 889B");
        aiNames.add("Minerva");
        aiNames.add("ZORAC");
        aiNames.add("The H2G2");
        aiNames.add("Deep Thought");
        aiNames.add("G.O.R:N.");
        aiNames.add("L7");
        aiNames.add("Project 2501");

        humanNames = Arrays.asList(("Adam Baum\n" +
                "Adam Zapel\n" +
                "Al Bino\n" +
                "Al Dente\n" +
                "Al Fresco\n" +
                "Al K. Seltzer\n" +
                "Alf A. Romeo\n" +
                "Ali Gaither\n" +
                "Ali Katt\n" +
                "Amanda Lynn\n" +
                "Amber Green\n" +
                "Andy Friese\n" +
                "Anita Bohn\n" +
                "Anita Dick\n" +
                "Anita Friske\n" +
                "Anita Goodman\n" +
                "Anita Hoare\n" +
                "Anita Job\n" +
                "Anita Knapp\n" +
                "Anita Lay\n" +
                "Anita Little\n" +
                "Anita Mann\n" +
                "Anita Mandalay\n" +
                "Anita Plummer\n" +
                "Anna Conda\n" +
                "Anna Fender (an offender)\n" +
                "Anna Graham\n" +
                "Anna Prentice (an apprentice)\n" +
                "Anna Recksiek (anorexic)\n" +
                "Anna Sasin\n" +
                "Anne Teak\n" +
                "Annette Curtain\n" +
                "Annie Howe\n" +
                "Annie Matter\n" +
                "Aretha Holly\n" +
                "Armand Hammer\n" +
                "Art Major\n" +
                "Art Painter\n" +
                "Art Sellers\n" +
                "Athol\n" +
                "B.A. Ware\n" +
                "Barb Dwyer\n" +
                "Barb E. Dahl\n" +
                "Barbara Seville\n" +
                "Barry Cade\n" +
                "Bea Minor and Dee Major\n" +
                "Beau Archer\n" +
                "Beau Tye\n" +
                "Ben Dover\n" +
                "Ben Down\n" +
                "Eileen Dover\n" +
                "Skip Dover\n" +
                "Ben Marcata (a musical term)\n" +
                "Bess Eaton (donut shop chain)\n" +
                "Biff Wellington\n" +
                "Bill Board\n" +
                "Bill Ding\n" +
                "Bill Foldes\n" +
                "Bill Loney\n" +
                "Billy Rubin\n" +
                "Bob Apple\n" +
                "Bob Katz\n" +
                "Tom Katz\n" +
                "Kitty Katz\n" +
                "Bonnie Ann Clyde\n" +
                "Bonnie Beaver\n" +
                "MD - she's an Ob/Gyn of course!\n" +
                "Brad Hammer (carpenter joke)\n" +
                "Brandon Cattell\n" +
                "Brandon Irons\n" +
                "Brandy Anne Koch (Brandy and Coke)\n" +
                "Brandy D. Cantor\n" +
                "Brighton Early\n" +
                "Brock Lee\n" +
                "Brooke Trout\n" +
                "Bud Light\n" +
                "Buster Cherry\n" +
                "Buster Hyman\n" +
                "C. Senor\n" +
                "C. Worthy\n" +
                "Cam Payne\n" +
                "Candace Spencer\n" +
                "Candy Barr\n" +
                "Candy Baskett\n" +
                "Candy Kane\n" +
                "Candy Sweet\n" +
                "Cara Van\n" +
                "Carrie Dababi (\"carry the baby\" - Dababi is an Egyptian name)\n" +
                "Carrie Oakey\n" +
                "Casey Macy\n" +
                "Charity Case\n" +
                "Cheri Pitts\n" +
                "Harry Pitts\n" +
                "Chip Munk\n" +
                "Chip Stone (sculptor)\n" +
                "Chris Coe\n" +
                "Chris Cross\n" +
                "Chris P. Bacon\n" +
                "Chuck U. Farley\n" +
                "Chuck Waggon\n" +
                "Claire Annette Reed\n" +
                "Constance Noring\n" +
                "Corey Ander\n" +
                "Corey O. Graff\n" +
                "Count Dunn\n" +
                "Count Orff\n" +
                "Craven Moorehead\n" +
                "Crystal Claire Waters\n" +
                "Crystal Glass\n" +
                "Crystal Metheney\n" +
                "Crystal Snow\n" +
                "D. Kay\n" +
                "DDS\n" +
                "D. Liver\n" +
                "Dan D. Lyons\n" +
                "Dan Deline\n" +
                "Dan Druff\n" +
                "Dan Saul Knight\n" +
                "Darren Deeds\n" +
                "Daryl Rhea\n" +
                "Dick Burns\n" +
                "Dick Face\n" +
                "Dick Head\n" +
                "Dick Hertz\n" +
                "Dick Long\n" +
                "Dick Mussell\n" +
                "Dick Pound\n" +
                "Dick Swett\n" +
                "Dick Tator\n" +
                "Dickson Yamada\n" +
                "Dilbert Pickles\n" +
                "Dinah Soares\n" +
                "Don Key\n" +
                "Donald Duck\n" +
                "Donny Brook\n" +
                "Doris Schutt\n" +
                "Doris Open\n" +
                "Doug Graves\n" +
                "Doug Hole\n" +
                "Doug & Phil Updegrave\n" +
                "Doug Witherspoon\n" +
                "Douglas Furr\n" +
                "Dr. Croak\n" +
                "Dr. Butcher\n" +
                "Dr. DeKay\n" +
                "Dr. E. Ville\n" +
                "Dr. Gutstein\n" +
                "Dr. Hanus\n" +
                "Dr. Hymen\n" +
                "Dr. Kauff\n" +
                "Dr. Pullham\n" +
                "Dr. Slaughter\n" +
                "Duane Pipe\n" +
                "Dusty Carr\n" +
                "Dusty Rhodes\n" +
                "Edna May\n" +
                "Earl E. Bird\n" +
                "Earl Lee Riser\n" +
                "Easton West\n" +
                "Weston East\n" +
                "Eaton Wright\n" +
                "Liv Good\n" +
                "Edward Z. Filler\n" +
                "Ella Vader\n" +
                "Emma Royds\n" +
                "Eric Shinn\n" +
                "Estelle Hertz\n" +
                "Evan Keel\n" +
                "Faith Christian\n" +
                "Fanny O'Rear\n" +
                "Fanny Hertz\n" +
                "Father A. Long\n" +
                "Ferris Wheeler\n" +
                "Flint Sparks\n" +
                "Ford Parker\n" +
                "Forrest Green\n" +
                "Foster Child\n" +
                "Frank Enstein\n" +
                "Gaye Barr\n" +
                "Holly Jolly\n" +
                "Wendy Storm\n" +
                "Rory Storm\n" +
                "Dusty Storm\n" +
                "Gene Poole\n" +
                "Geoff L. Tavish\n" +
                "Gil Fish\n" +
                "Ginger Rayl\n" +
                "Ginger Snapp\n" +
                "Ginger Vitus\n" +
                "Gladys C. Hughes\n" +
                "H. Wayne Carver\n" +
                "Hamilton Burger\n" +
                "Harden Thicke\n" +
                "Harold Assman\n" +
                "Harry Armand Bach\n" +
                "Harry Beard\n" +
                "Harry Beaver\n" +
                "Harry Butts\n" +
                "Harry Chest\n" +
                "Harry Cox\n" +
                "Harry Dangler\n" +
                "Harry Johnson\n" +
                "Harry Legg\n" +
                "Harry Hooker\n" +
                "Harry P. Ness\n" +
                "Harry Peters\n" +
                "Harry Lipp\n" +
                "Harry Sachs\n" +
                "Harry R. M. Pitts\n" +
                "Hazle Nutt\n" +
                "Heidi Clare\n" +
                "Helen Back\n" +
                "Helen Wiells\n" +
                "Herb Farmer\n" +
                "Herb Rice\n" +
                "Holly McRell\n" +
                "Holly Day\n" +
                "Holly Wood\n" +
                "Honey Bee\n" +
                "Howie Doohan\n" +
                "Hugh Jass\n" +
                "Hugh Jorgan\n" +
                "Hugh Morris\n" +
                "Hy Ball\n" +
                "Hy Lowe\n" +
                "Bea Lowe\n" +
                "Hy Marx\n" +
                "Hy Price\n" +
                "I.D. Clair\n" +
                "I. Lasch\n" +
                "I.M. Boring\n" +
                "I.P. Freely\n" +
                "I.P. Daly\n" +
                "Ileane Wright\n" +
                "Ilene South" +
                "Iona Ford\n" +
                "Iona Frisbee\n" +
                "Ivan Oder\n" +
                "Ivy Leage\n" +
                "Jack Hoff\n" +
                "Jack Haas\n" +
                "Jack Hammer\n" +
                "Jack Knoff\n" +
                "Jack Pott\n" +
                "Jack Tupp\n" +
                "Jacklyn Hyde\n" +
                "Jasmine Rice\n" +
                "Jay Walker\n" +
                "Jean Poole\n" +
                "Jed Dye (Jedi)\n" +
                "Jenny Tull\n" +
                "Jerry Atrick\n" +
                "Jim Laucher\n" +
                "Jim Shorts\n" +
                "Jim Shu\n" +
                "Jim Sox\n" +
                "Jo King\n" +
                "Joe Kerr\n" +
                "Jordan Rivers\n" +
                "Joy Kil\n" +
                "Joy Rider\n" +
                "June Bugg\n" +
                "Justin Case\n" +
                "Justin Casey Howells\n" +
                "Justin Hale\n" +
                "Justin Inch\n" +
                "Justin Miles North\n" +
                "Justin Sane\n" +
                "Justin Time,\n" +
                "Kandi Apple\n" +
                "Kay Bull\n" +
                "Kelly Green\n" +
                "Ken Dahl\n" +
                "Kenny Penny\n" +
                "Kenya Dewit\n" +
                "Kerry Oki\n" +
                "King Queene\n" +
                "Lance Boyle\n" +
                "Lance Butts\n" +
                "Laura Lynne Hardy\n" +
                "Laurel Ann Hardy\n" +
                "Laura Norder\n" +
                "Laurence Getzoff\n" +
                "Leigh King\n" +
                "Les Moore\n" +
                "Les Payne\n" +
                "Levon Coates\n" +
                "Lewis N. Clark\n" +
                "Lily Pond\n" +
                "Lindsay Doyle\n" +
                "Lisa Carr\n" +
                "Kitty Carr\n" +
                "Otto Carr\n" +
                "Parker Carr\n" +
                "Lisa Ford\n" +
                "Lisa Honda\n" +
                "Iona Corolla\n" +
                "Lisa May Boyle\n" +
                "Lisa May Dye\n" +
                "Liv Long\n" +
                "Lois Price\n" +
                "Lou Pole\n" +
                "Lou Zar (loser)\n" +
                "Lucy Fer\n" +
                "Luke Warm\n" +
                "Lynn C. Doyle\n" +
                "Lynn O. Liam\n" +
                "M. Balmer\n" +
                "Mark Skid (Skid\n" +
                "Mark)\n" +
                "Manny Kinn\n" +
                "Marlon Fisher\n" +
                "Marsha Dimes\n" +
                "Marsha Mellow\n" +
                "Marshall Law\n" +
                "Marty Graw\n" +
                "Mary Annette Woodin\n" +
                "Mary A. Richman\n" +
                "Mary Christmas\n" +
                "Matt Tress\n" +
                "Maude L.T. Ford\n" +
                "Max Little\n" +
                "Max Power\n" +
                "May Day\n" +
                "May Furst\n" +
                "Mel Loewe\n" +
                "Melody Music\n" +
                "Mike Easter\n" +
                "Mike Hunt\n" +
                "Mike Raffone\n" +
                "Mike Reinhart\n" +
                "Mike Rotch\n" +
                "Mike Sweeney\n" +
                "Milly Graham\n" +
                "Minny van Gogh\n" +
                "Missy Sippy\n" +
                "Mrs. Sippy\n" +
                "Mister Bates\n" +
                "Rocky Shore\n" +
                "Mo Lestor\n" +
                "Moe B. Dick\n" +
                "Molly Kuehl\n" +
                "Mona Lott\n" +
                "Morey Bund\n" +
                "Myles Long\n" +
                "Nancy Ann Cianci\n" +
                "Nat Sass\n" +
                "Neil Down\n" +
                "Neil Crouch\n" +
                "Nick O. Time\n" +
                "Noah Riddle\n" +
                "Noah Lott\n" +
                "Norma Leigh Lucid\n" +
                "Olive Branch\n" +
                "Olive Green\n" +
                "Olive Hoyl\n" +
                "Olive Yew (I love you)\n" +
                "Oliver Sutton\n" +
                "Ophelia Payne\n" +
                "Oren Jellow\n" +
                "Orson Carte\n" +
                "Oscar Ruitt\n" +
                "Otto Graf\n" +
                "Owen Big\n" +
                "Owen Cash\n" +
                "Owen Moore\n" +
                "P. Ness\n" +
                "A. Ness\n" +
                "P. Brain\n" +
                "Paige Turner\n" +
                "Park A. Studebaker\n" +
                "Pat McCann\n" +
                "Pat Hiscock\n" +
                "Pearl Button\n" +
                "Pearl E. Gates\n" +
                "Pearl E. White\n" +
                "Peg Legge\n" +
                "Penny Dollar\n" +
                "Bill Dollar\n" +
                "Penny Lane\n" +
                "Penny Nichols\n" +
                "Penny Profit\n" +
                "Penny Wise\n" +
                "Pepe Roni\n" +
                "Dick Johnson\n" +
                "Peter Peed\n" +
                "Peter Wacko\n" +
                "Phil Bowles\n" +
                "Phil Rupp\n" +
                "Phil Wright\n" +
                "Phillip D. Bagg\n" +
                "Pierce Cox\n" +
                "Pierce Deere\n" +
                "Pierce Hart\n" +
                "Polly Ester\n" +
                "Post\n" +
                "Mark\n" +
                "R. M. Pitt\n" +
                "R. Sitch\n" +
                "R. Slicker\n" +
                "Randy Guy\n" +
                "Randy Lover\n" +
                "Raney Schauer\n" +
                "Ray Gunn\n" +
                "Ray Zenz (raisins)\n" +
                "Raynor Schein\n" +
                "Reid Enright\n" +
                "Rhea Curran\n" +
                "Rhoda Booke\n" +
                "Rita Booke\n" +
                "Rich Feller\n" +
                "Rich Guy\n" +
                "Rich Kidd\n" +
                "Rich Mann\n" +
                "Rick O'Shea\n" +
                "Rick Shaw\n" +
                "Rip Torn\n" +
                "Rita Buch\n" +
                "Rita Story\n" +
                "Robin Andis Merryman\n" +
                "Robin Banks\n" +
                "Rob Banks\n" +
                "Robin Feathers\n" +
                "Robin Money\n" +
                "U. O. Money\n" +
                "Rock (Rocco) Bottoms\n" +
                "Rock Pounder\n" +
                "Rock Stone\n" +
                "Rocky Beach\n" +
                "Sandy Beach\n" +
                "Rocky Mountain\n" +
                "Cliff Mountain\n" +
                "Rocky Rhoades\n" +
                "Rod N. Reel\n" +
                "Roman Holiday\n" +
                "Rose Bush\n" +
                "Rose Gardner\n" +
                "Rowan Boatman\n" +
                "Royal Payne\n" +
                "Russell Leeves\n" +
                "Russell Sprout\n" +
                "Rusty Blades\n" +
                "Rusty Bridges\n" +
                "Rusty Carr\n" +
                "Rusty Dorr\n" +
                "Rusty Fossat\n" +
                "Rusty Fender\n" +
                "Rusty Irons\n" +
                "Rusty Keyes\n" +
                "Rusty Nail\n" +
                "Rusty Pipes\n" +
                "Rusty Steele\n" +
                "Ryan Carnation\n" +
                "Ryan Coke\n" +
                "Sal A. Mander\n" +
                "Sal Minella\n" +
                "Sam Manilla\n" +
                "Sally Forth\n" +
                "Sarah Bellum\n" +
                "Sawyer B. Hind\n" +
                "Sawyer Dickey\n" +
                "Sandy Banks\n" +
                "Sandy Beech\n" +
                "Sandy Brown\n" +
                "Sandy Spring\n" +
                "Seth Poole\n" +
                "Sharon Fillerup\n" +
                "Sharon Needles\n" +
                "Sharon Weed\n" +
                "Sharon A. Burger\n" +
                "Sheila Blige\n" +
                "Skip Roper\n" +
                "Skip Stone\n" +
                "Sonny Day\n" +
                "Stan Still\n" +
                "Stanley Cupp\n" +
                "Sue Flay\n" +
                "Sue Render\n" +
                "Sue Ridge\n" +
                "Sue Yu\n" +
                "Sue Jeu\n" +
                "Summer Day\n" +
                "Summer Greene\n" +
                "Summer Holiday\n" +
                "Sy Burnette\n" +
                "Tad Moore\n" +
                "Tad Pohl\n" +
                "Tamara Knight\n" +
                "Tanya Hyde\n" +
                "Ted E. Baer\n" +
                "Tess Steckle\n" +
                "Therese R. Green\n" +
                "Teresa Green\n" +
                "Thomas Richard Harry\n" +
                "Tim Burr\n" +
                "Tish Hughes\n" +
                "Tom A. Toe\n" +
                "Tom Katt\n" +
                "Tom Morrow\n" +
                "Tommy Gunn\n" +
                "Tommy Hawk\n" +
                "Trina Woods\n" +
                "Trina Forest\n" +
                "Ty Coon\n" +
                "Ty Knotts\n" +
                "Urich Hunt\n" +
                "Viola Solo\n" +
                "Virginia Beach\n" +
                "Walter Melon\n" +
                "Wanda Rinn\n" +
                "Wanna Hickey\n" +
                "Warren Peace\n" +
                "Warren T.\n" +
                "Will Power\n" +
                "Will Wynn (Mayor of Austin\n" +
                "Texas)\n" +
                "Willie B. Hardigan\n" +
                "Willie Leak\n" +
                "Willie Stroker\n" +
                "Willie Waite\n" +
                "Winsom Cash\n" +
                "Owen Cash\n" +
                "Woody Forrest\n" +
                "X. Benedict").split("\n"));

        // shuffle
        shuffleAINames();
        shuffleHumanNames();
    }

    private static void shuffleAINames(){
        long seed = System.nanoTime();
        Collections.shuffle(aiNames, new Random(seed));
    }

    private static void shuffleHumanNames(){
        long seed = System.nanoTime();
        Collections.shuffle(humanNames, new Random(seed));
    }

    public static String getNextAIName() {
        lastAIIndex++;
        if (lastAIIndex >= aiNames.size()) {
            shuffleAINames();
            lastAIIndex = 0;
        }

        return aiNames.get(lastAIIndex);
    }

    public static int getNumberOfAvailableAINames() {
        return aiNames.size();
    }

    public static String getNextHumanName() {
        lastHumanIndex++;
        if (lastHumanIndex >= humanNames.size()) {
            shuffleHumanNames();
            lastHumanIndex = 0;
        }

        return humanNames.get(lastHumanIndex);
    }

    public static int getNumberOfAvailableHumanNames() {
        return humanNames.size();
    }
}
