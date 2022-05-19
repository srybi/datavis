package de.th.ro.datavis.interpreter.calc;

import java.math.BigDecimal;

import de.th.ro.datavis.interfaces.ICalc;
import de.th.ro.datavis.models.FFSLine;
import de.th.ro.datavis.util.enums.InterpretationMode;

public class Calc {

    public static double x_polarToCartesian(FFSLine line, InterpretationMode mode) {
        return calcIntensity(line.getRePhi(), line.getImPhi(), line.getReTheta(), line.getImTheta(), mode)
                * Math.sin(line.getTheta())
                * Math.cos(line.getPhi());
    }

    public static double y_polarToCartesian(FFSLine line, InterpretationMode mode) {
        return calcIntensity(line.getRePhi(), line.getImPhi(), line.getReTheta(), line.getImTheta(), mode)
                * Math.sin(line.getTheta())
                * Math.sin(line.getPhi());
    }

    public static double z_polarToCartesian(FFSLine line, InterpretationMode mode) {
        return calcIntensity(line.getRePhi(), line.getImPhi(), line.getReTheta(), line.getImTheta(), mode)
                * Math.cos(line.getTheta());
    }

    public static double calcIntensity(FFSLine line, InterpretationMode mode) {
        return calcIntensity(line.getRePhi(), line.getImPhi(), line.getReTheta(), line.getImTheta(), mode);
    }

    // Todo Custom Model
    public static double calcIntensity(double realPhi, double imaginaryPhi, double realTheta, double imaginaryTheta, InterpretationMode mode) {
        double squaredResult = Math.pow(realPhi, 2) + Math.pow(imaginaryPhi, 2) + Math.pow(realTheta, 2) + Math.pow(imaginaryTheta, 2);

        if(mode == InterpretationMode.Linear){
            return Math.sqrt(squaredResult);
        }else{
            return Math.log10(Math.sqrt(squaredResult));
        }
    }
}
