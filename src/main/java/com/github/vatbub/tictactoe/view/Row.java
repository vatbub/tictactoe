package com.github.vatbub.tictactoe.view;

import java.util.List;

/**
 * A row in the gameTable view
 */
public class Row {
    private List<String> values;

    public Row(List<String> values){
        setValues(values);
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
