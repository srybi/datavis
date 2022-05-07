package de.th.ro.datavis.interpreter.calc;

import java.math.BigDecimal;

import de.th.ro.datavis.interfaces.ICalc;
import de.th.ro.datavis.models.FFSLine;
import de.th.ro.datavis.models.PolarCoordinates;

public class Calc {

    public static double x_polarToCartesian(FFSLine line) {
        return calcIntensity(line.getRePhi(), line.getImPhi(), line.getReTheta(), line.getImTheta())
                * Math.sin(line.getTheta())
                * Math.cos(line.getPhi());
    }

    public static double y_polarToCartesian(FFSLine line) {
        return calcIntensity(line.getRePhi(), line.getImPhi(), line.getReTheta(), line.getImTheta())
                * Math.sin(line.getTheta())
                * Math.sin(line.getPhi());
    }

    public static double z_polarToCartesian(FFSLine line) {
        return calcIntensity(line.getRePhi(), line.getImPhi(), line.getReTheta(), line.getImTheta())
                * Math.cos(line.getTheta());
    }


    // Todo Custom Model
    public static double calcIntensity(double realPhi, double imaginaryPhi, double realTheta, double imaginaryTheta) {
        double squaredResult = Math.pow(realPhi, 2) + Math.pow(imaginaryPhi, 2) + Math.pow(realTheta, 2) + Math.pow(imaginaryTheta, 2);
        return Math.sqrt(squaredResult);
    }

}
