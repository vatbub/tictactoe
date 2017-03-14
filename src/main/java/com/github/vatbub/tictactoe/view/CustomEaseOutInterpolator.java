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
 * {@code Interpolator} for cubic 1d interpolation
 */
@SuppressWarnings("WeakerAccess")
public class CustomEaseOutInterpolator extends Interpolator {
    private double S4;
    private double x0;

    public CustomEaseOutInterpolator(double S4, double x0) {
        setS4(S4);
        setX0(x0);
    }

    private static double clamp(double t) {
        return (t < 0.0) ? 0.0 : (t > 1.0) ? 1.0 : t;
    }

    @Override
    protected double curve(double t) {
        double S1 = (getS4() - 1) / (-Math.pow(getX0(), 2) + 2 * getX0() - 1);
        double S2 = getS4() - 2 * S1 * getX0();
        double S3 = 1 - S2 - S1;
        return clamp((t > getX0()) ? S1 * t * t + S2 * t + S3 : getS4()
                * t);
    }

    public double getS4() {
        return S4;
    }

    public void setS4(double S4) {
        this.S4 = S4;
    }

    public double getX0() {
        return x0;
    }

    public void setX0(double x0) {
        this.x0 = x0;
    }
}
