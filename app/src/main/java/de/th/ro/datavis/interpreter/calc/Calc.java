package de.th.ro.datavis.interpreter.calc;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Arrays;

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

    public static int calcHammingDistance(String x, String y){

        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    public static int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }
}
