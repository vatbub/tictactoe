package com.github.vatbub.tictactoe.view;

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
