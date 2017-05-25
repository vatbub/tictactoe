package com.github.vatbub.tictactoe.view.refreshables;

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


import javafx.scene.shape.Line;

/**
 * A {@link Refreshable} {@code Line}
 */
public abstract class RefreshableLine extends Line implements Refreshable {
    /**
     * Creates an empty instance of Line.
     */
    public RefreshableLine() {
    }

    /**
     * Creates a new instance of Line.
     *
     * @param startX the horizontal coordinate of the start point of the line segment
     * @param startY the vertical coordinate of the start point of the line segment
     * @param endX   the horizontal coordinate of the end point of the line segment
     * @param endY   the vertical coordinate of the end point of the line segment
     */
    public RefreshableLine(double startX, double startY, double endX, double endY) {
        super(startX, startY, endX, endY);
    }
}
