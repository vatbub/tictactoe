package com.github.vatbub.tictactoe.view;

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
