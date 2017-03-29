package com.github.vatbub.tictactoe.view;

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


import javafx.animation.Interpolator;

/**
 * EASE-OUT-{@code Interpolator} with custom ease out point and ease out slope
 */
public class CustomEaseBothInterpolator extends Interpolator {
    private double x1;
    private double x2;

    public CustomEaseBothInterpolator(double x1, double x2) {
        setX1(x1);
        setX2(x2);
    }

    @Override
    protected double curve(double t) {
        // See the SMIL 3.1 specification for details on this calculation
        // acceleration = 0.2, deceleration = 0.2
        double a1 = -1 / (getX1() * (-getX2() - 1) + Math.pow(getX1(), 2));
        double a2 = -2 / (-getX2() + getX1() - 1);
        double b2 = getX1() / (-getX2() + getX1() - 1);
        double a3 = -1 / (-Math.pow(getX2(), 2) + getX1() * (getX2() - 1) + 1);
        double b3 = 2 / (-Math.pow(getX2(), 2) + getX1() * (getX2() - 1) + 1);
        double c3 = (getX1() * (getX2() - 1) - Math.pow(getX2(), 2)) / (-Math.pow(getX2(), 2) + getX1() * (getX2() - 1) + 1);
        return clamp((t < x1) ? a1 * t * t
                : (t > x2) ? a3 * t * t + b3 * t + c3
                : a2 * t + b2);
    }

    private double clamp(double t) {
        return (t < 0.0) ? 0.0 : (t > 1.0) ? 1.0 : t;
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }
}
