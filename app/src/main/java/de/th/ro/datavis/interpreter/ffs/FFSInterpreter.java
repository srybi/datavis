package de.th.ro.datavis.interpreter.ffs;

import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import de.th.ro.datavis.interfaces.IInterpreter;
import de.th.ro.datavis.interpreter.calc.Calc;
import de.th.ro.datavis.models.FFSLine;
import de.th.ro.datavis.models.Sphere;
import de.th.ro.datavis.util.enums.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class FFSInterpreter implements IInterpreter {

    private static final int startingLine = 32;
    private static final String LOG_TAG = "Interpretation";
    private double averageIntensity = 0;

    public FFSInterpreter() {}


    @Override
    public List<Sphere> interpretData(InputStream stream, double scalingFactor, InterpretationMode mode) throws FFSInterpretException {
            InputStreamReader reader = new InputStreamReader(stream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            return interpretData(bufferedReader, scalingFactor, mode);

    }

    @Override
    public List<Sphere> interpretData(File file, double scalingFactor, InterpretationMode mode) throws FFSInterpretException {

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            return interpretData(bufferedReader, scalingFactor, mode);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<Sphere>();
        }
    }


    private List<Sphere> interpretData(BufferedReader reader, double scalingFactor, InterpretationMode mode) throws FFSInterpretException {
        Log.d(LOG_TAG, "Start Interpretation...");
        List<Sphere> coordinates;
        try {
            List<FFSLine> ffsLines = reader.lines()
                    .skip(startingLine - 1)
                    .limit(2701)
                    .map(x -> {
                        String[] vals = x.trim().split("\\s+");
                        double[] dVals = Arrays.stream(vals).mapToDouble(Double::parseDouble).toArray();

                        return new FFSLine(dVals[0], dVals[1], dVals[2], dVals[3], dVals[4], dVals[5]);
                    }).collect(Collectors.toList());

            coordinates = ffsLines.stream().map(line -> {

                double x = Calc.x_polarToCartesian(line, mode);
                double y = Calc.y_polarToCartesian(line, mode);
                double z = Calc.z_polarToCartesian(line, mode);
                double intensity = Calc.calcIntensity(line, mode);
                averageIntensity += intensity;
                return new Sphere(x*scalingFactor, y*scalingFactor, z*scalingFactor, intensity);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            //TODO: Specify exceptions, which can be thrown during the interpretation
            throw new FFSInterpretException(e.getMessage());
        }
        averageIntensity /= coordinates.size();

        Log.d(LOG_TAG, "Interpretation finished");
        return coordinates;
    }

    @Override
    public Color getIntensityColor(double intensity) {
        //TODO Farben wie in CST
        double delta = Math.abs(averageIntensity - intensity);

        if(delta > 2){
            //#d7191c red
            return new Color(0.84f, 0.1f, 0.11f);
        }
        if(delta > 0.5){
            //#fdae61 orange
            return new Color(0.99f, 0.68f, 0.38f);
        }
        if(delta > 0.25){
            //#ffffbf yellow
            return new Color(1f, 1f, 0.75f);
        }
        if(delta > 0.1){
            //#abd9e9 baby blue
            return new Color(0.67f, 0.85f, 0.91f);
        }
        //#2c7bb6 blue
        return new Color(0.17f, 0.48f, 0.71f);
    }
}