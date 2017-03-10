package com.github.vatbub.tictactoe;

import org.junit.Test;

/**
 * Tests for the {@link NameList} class
 */
public class NameListTest {
    @Test
    public void aiNamesOverflowTest(){
        for(int i = 0; i<2*NameList.getNumberOfAvailableAINames(); i++){
            System.out.println(NameList.getNextAIName());
        }

        // everything went good
        assert true;
    }

    @Test
    public void humanNamesOverflowTest(){
        for(int i = 0; i<2*NameList.getNumberOfAvailableHumanNames(); i++){
            System.out.println(NameList.getNextHumanName());
        }

        // everything went good
        assert true;
    }
}
