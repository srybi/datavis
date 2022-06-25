package de.th.ro.datavis.interpreter.ffs;

import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.th.ro.datavis.interfaces.IInterpreter;
import de.th.ro.datavis.interpreter.calc.Calc;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.FFSLine;
import de.th.ro.datavis.models.Result;
import de.th.ro.datavis.models.Sphere;
import de.th.ro.datavis.util.Helper;
import de.th.ro.datavis.util.constants.FFSConstants;
import de.th.ro.datavis.util.constants.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class FFSInterpreter implements IInterpreter {

    private static final String LOG_TAG = "Interpretation";
    private double maxItensity = -1;

    private static final int MAX_HAMMING_DISTANCE = 10;

    public FFSInterpreter() {}


    @Override
    public Result<Pair<ArrayList<AtomicField>, ArrayList<AtomicField>>> interpretData(InputStream stream, double scalingFactor, double tiltValue, int antennaId) throws FFSInterpretException {
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(reader);

        return interpretData(bufferedReader, scalingFactor, tiltValue, antennaId);
    }

    @Override
    public Result<Pair<ArrayList<AtomicField>, ArrayList<AtomicField>>> interpretData(File file, double scalingFactor, double tiltValue, int antennaId) throws FFSInterpretException {
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            return interpretData(bufferedReader, scalingFactor, tiltValue, antennaId);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Result.error("File Not Found.");
        }
    }


    private Result<Pair<ArrayList<AtomicField>, ArrayList<AtomicField>>> interpretData(BufferedReader reader, double scalingFactor, double tiltValue, int antennaId) throws FFSInterpretException {
        AtomicField atomicField;
        maxItensity = -1;
        Log.d(LOG_TAG, "Start Interpretation...");
        List<Sphere> coordinates;
        int frequencies = -1;
        int samples = -1;
        ArrayList<Double> frequencyValues = new ArrayList<>();
        boolean startFound = false;

        try {
            String line;
            while((line = reader.readLine()) != null && (frequencies == -1 || samples == -1 || !startFound || frequencyValues.size() < frequencies)) {
                if(Calc.calcLevenstheinDistance(line.trim(),(FFSConstants.FREQUENCIES_HEADER.trim())) < MAX_HAMMING_DISTANCE){
                    //go to next line to get the value
                    line = reader.readLine();
                    frequencies = Integer.parseInt(line.trim());
                }
                if(Calc.calcLevenstheinDistance(line.trim(),(FFSConstants.SAMPLES_HEADER.trim())) < MAX_HAMMING_DISTANCE){
                    //go to next line to get the value
                    line = reader.readLine();
                    // first value: phi samples; second value: theta samples
                    int[] vals = Arrays.stream(line.trim().split("\\s+")).mapToInt(Integer::parseInt).toArray();
                    samples = vals[0]*vals[1];
                }
                if(Calc.calcLevenstheinDistance(line.trim(),(FFSConstants.VALUES_HEADER.trim())) < MAX_HAMMING_DISTANCE){
                    startFound = true;
                    break;
                }
                if(Calc.calcLevenstheinDistance(line.trim(),(FFSConstants.RADACCSTMFREQ_HEADER.trim())) < MAX_HAMMING_DISTANCE){
                    try{
                        frequencyValues = extractFrequencyValues(reader, frequencies);}
                    catch(Exception e){
                        e.printStackTrace();
                        return Result.error("Could not extract frequency values.");
                    }
                }
            }

            //Check if all headers were found and values could be extracted
            if(frequencies == -1 || samples == -1 || !startFound || frequencyValues.size() < frequencies)
                return Result.error("Could not find headers in FFS file.");


            ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
            for (int i = 0; i < frequencies; i++) {
                values.add(readAtomicField(reader, samples));

                //Dont search for next atomicfield if we are at the last one
                if(i < frequencies - 1)
                    findNextAtomicField(reader);
            }

            ArrayList<AtomicField> atomicFieldsLog = interpretValues(values,frequencyValues, tiltValue, scalingFactor, InterpretationMode.Logarithmic, antennaId);
            ArrayList<AtomicField> atomicFieldsLin = interpretValues(values,frequencyValues, tiltValue, scalingFactor, InterpretationMode.Linear, antennaId);
            Log.d(LOG_TAG, "Interpretation finished");
            return Result.success(Pair.create(atomicFieldsLog, atomicFieldsLin));

        } catch (Exception e) {
            throw new FFSInterpretException(e.getMessage());
        }
    }

    private ArrayList<Double> extractFrequencyValues(BufferedReader reader, int frequencies) throws IOException {
        int valuesPerFreq = 4 + 1; // 4 values per frequency + 1 for a empty line
        ArrayList<Double> frequencyValues = new ArrayList<>();
        for (int i = 0; i < frequencies; i++) {
            for (int j = 0; j < valuesPerFreq; j++) {
                if (j == 3) {
                    frequencyValues.add(Double.parseDouble(reader.readLine().trim()));
                }else{
                    reader.readLine();
                }

            }
        }
        return frequencyValues;
    }

    private ArrayList<AtomicField> interpretValues(ArrayList<ArrayList<String>> values, ArrayList<Double> frequencies, double tilt, double scalingFactor, InterpretationMode mode, int antennaId) throws FFSInterpretException {
        ArrayList<AtomicField> atomicFields = new ArrayList<>();
        for(Pair<ArrayList<String>, Double> pair : Helper.zip(values, frequencies)){
            Result<AtomicField> atomicField = interpretValue(pair.first, scalingFactor, mode);
            if (!atomicField.isSuccess()) {
                throw new FFSInterpretException(atomicField.getMessage());
            }
            AtomicField field = atomicField.getData();
            //convert to gHz
            double frequency = pair.second/1000000000;
            field.setFrequency(frequency);
            field.setTilt(tilt);
            field.setAntennaId(antennaId);
            atomicFields.add(field);
        }
        return atomicFields;
    }

    private Result<AtomicField> interpretValue(ArrayList<String> value, double scalingFactor, InterpretationMode mode) throws FFSInterpretException {
        return interpretDataAsStream(value.stream(),scalingFactor,mode);
    }

    private void findNextAtomicField(BufferedReader reader) {
        String line;
        try {
            while((line = reader.readLine()) != null){
                if(Calc.calcLevenstheinDistance(line.trim(),(FFSConstants.VALUES_HEADER.trim())) < MAX_HAMMING_DISTANCE){
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> readAtomicField(BufferedReader reader, int samples) {
        ArrayList<String> values = new ArrayList<String>();
        String line;
        try {
            for (int i = 0; i < samples; i++) {
                line = reader.readLine();
                values.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return values;
    }

    @Override
    public Result<AtomicField> interpretDataAsStream(Stream<String> stream, double scalingFactor, InterpretationMode mode) throws FFSInterpretException {
        maxItensity = -1;
        AtomicField atomicField = new AtomicField(2,1,mode, new ArrayList<>(),maxItensity , 1, 1);
        List<Sphere> coordinates;
        AtomicBoolean error = new AtomicBoolean(false);
        List<FFSLine> ffsLines = stream
                .map(x -> {
                    FFSLine line = null;
                    try{
                        String[] vals = x.trim().split("\\s+");
                        double[] dVals = Arrays.stream(vals).mapToDouble(Double::parseDouble).toArray();

                        line = new FFSLine(dVals[0], dVals[1], dVals[2], dVals[3], dVals[4], dVals[5]);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        error.set(true);
                    }
                    return line;
                }).collect(Collectors.toList());

        if(error.get()){
            return Result.error(".ffs File contains corrupt values.");
        }

        //Bugfix: If stepsize of file is greater than smaller than 3, the interpreter will use a stepsize of 3
        final int stepSize = (ffsLines.get(1).getTheta() - ffsLines.get(0).getTheta()) < 3 ? 3 : 1;

        //Bugfix: filter all negative intensities
        coordinates = ffsLines.stream().filter(l -> (l.getPhi()%stepSize == 0) && (l.getTheta()%stepSize == 0)).filter(l -> Calc.calcIntensity(l, InterpretationMode.Logarithmic) > 0).map(l -> {

            double x = Calc.x_polarToCartesian(l, mode);
            double y = Calc.y_polarToCartesian(l, mode);
            double z = Calc.z_polarToCartesian(l, mode);
            double intensity = Calc.calcIntensity(l, mode);
            if(intensity > atomicField.maxIntensity){
                atomicField.maxIntensity = intensity;
            }
            return new Sphere(x, y, z, intensity);
        }).collect(Collectors.toList());

        atomicField.setSpheres(coordinates);
        return Result.success(atomicField);
    }
}