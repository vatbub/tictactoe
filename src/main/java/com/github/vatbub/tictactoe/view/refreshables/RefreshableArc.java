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


import javafx.scene.shape.Arc;

/**
 * A {@link Refreshable} {@code Arc}
 */
public abstract class RefreshableArc extends Arc implements Refreshable {
    /**
     * Creates an empty instance of Arc.
     */
    public RefreshableArc() {
    }

    /**
     * Creates a new instance of Arc.
     *
     * @param centerX    the X coordinate of the center point of the arc
     * @param centerY    the Y coordinate of the center point of the arc
     * @param radiusX    the overall width (horizontal radius) of the full ellipse
     *                   of which this arc is a partial section
     * @param radiusY    the overall height (vertical radius) of the full ellipse
     *                   of which this arc is a partial section
     * @param startAngle the starting angle of the arc in degrees
     * @param length     the angular extent of the arc in degrees
     */
    public RefreshableArc(double centerX, double centerY, double radiusX, double radiusY, double startAngle, double length) {
        super(centerX, centerY, radiusX, radiusY, startAngle, length);
    }
}
