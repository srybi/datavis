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

    private static final String LOG_TAG = "Interpretation";
    private double averageIntensity = 0;

    private static final int MAX_HAMMING_DISTANCE = 10;

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
        int frequencies = -1;
        int samples = -1;
        boolean startFound = false;

        try {
            String line;
            while((line = reader.readLine()) != null && (frequencies == -1 || samples == -1 || !startFound)){
                if(Calc.calcHammingDistance(line.trim(),(FFSConstants.FREQUENCIES_HEADER.trim())) < MAX_HAMMING_DISTANCE){
                    //go to next line to get the value
                    line = reader.readLine();
                    frequencies = Integer.parseInt(line.trim());
                }
                if(Calc.calcHammingDistance(line.trim(),(FFSConstants.SAMPLES_HEADER.trim())) < MAX_HAMMING_DISTANCE){
                    //go to next line to get the value
                    line = reader.readLine();
                    // first value: phi samples; second value: theta samples
                    int[] vals = Arrays.stream(line.trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
                    samples = vals[0]*vals[1];
                }
                if(Calc.calcHammingDistance(line.trim(),(FFSConstants.VALUES_HEADER.trim())) < MAX_HAMMING_DISTANCE){
                    startFound = true;
                    break;
                }
            }

            //TODO: Currently the first frequency is chosen. This should be specified in the parameter list
            List<FFSLine> ffsLines = reader.lines()
                    .limit(samples)
                    .map(x -> {
                        String[] vals = x.trim().split("\\s+");
                        double[] dVals = Arrays.stream(vals).mapToDouble(Double::parseDouble).toArray();

                        return new FFSLine(dVals[0], dVals[1], dVals[2], dVals[3], dVals[4], dVals[5]);
                    }).collect(Collectors.toList());

            coordinates = ffsLines.stream().map(l -> {

                double x = Calc.x_polarToCartesian(l, mode);
                double y = Calc.y_polarToCartesian(l, mode);
                double z = Calc.z_polarToCartesian(l, mode);
                double intensity = Calc.calcIntensity(l, mode);
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
            //#FE0000 red
            return new Color(1f, 0f, 0f);
        }
        if(delta > 0.5){
            //#FF1178 pink
            return new Color(1f, 0.07f, 0.47f);
        }
        if(delta > 0.25){
            //#FFF205 yellow
            return new Color(1f, 0.95f, 0.02f);
        }
        if(delta > 0.1){
            //#01FFF4  blue
            return new Color(0f, 1f, 0.96f);
        }
        //#7CFF01 green
        return new Color(0.49f, 1f, 0f);
    }
}