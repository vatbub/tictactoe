package com.github.vatbub.tictactoe.common;

/*-
 * #%L
 * tictactoe.common
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


import java.util.Objects;

public class Move {
    private final int column;
    private final int row;

    public Move(int row, int column) {
        this.column = column;
        this.row = row;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return getColumn() == move.getColumn() &&
                getRow() == move.getRow();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getColumn(), getRow());
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "row: " + getRow() + ", col: " + getColumn();
    }

    public int getRow() {
        return row;
    }
}
