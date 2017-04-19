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


import org.junit.Test;

/**
 * Tests for the {@link NameList} class
 */
public class NameListTest {
    @Test
    public void firstNamesOverflowTest(){
        for(int i = 0; i<2*NameList.getNumberOfAvailableFirstNames(); i++){
            System.out.println(NameList.getNextFirstName());
        }

        // everything went good
        assert true;
    }

    @Test
    public void lastNamesOverflowTest(){
        for(int i = 0; i<2*NameList.getNumberOfAvailableLastNames(); i++){
            System.out.println(NameList.getNextLastName());
        }

        // everything went good
        assert true;
    }
}
