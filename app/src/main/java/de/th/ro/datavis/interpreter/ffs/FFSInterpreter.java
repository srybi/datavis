package de.th.ro.datavis.interpreter.ffs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.ar.sceneform.math.Vector3;

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
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class FFSInterpreter implements IInterpreter {

    private static final int startingLine = 32;

    public FFSInterpreter() {}

    @Override
    public List<Vector3> interpretData(InputStream stream, double scalingFactor) throws FFSInterpretException {
            InputStreamReader reader = new InputStreamReader(stream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            return interpretData(bufferedReader, scalingFactor);

    }

    @Override
    public List<Vector3> interpretData(File file, double scalingFactor) throws FFSInterpretException {

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            return interpretData(bufferedReader, scalingFactor);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<Vector3>();
        }


    }

    private List<Vector3> interpretData(BufferedReader reader, double scalingFactor) throws FFSInterpretException {
            List<FFSLine> ffsLines = reader.lines()
                    .skip(startingLine - 1)
                    .limit(2600)
                    .map(x -> {
                        String [] vals = x.trim().split("\\s+");
                        double [] dVals = Arrays.stream(vals).mapToDouble(Double::parseDouble).toArray();

                        return new FFSLine(dVals[0], dVals[1], dVals[2], dVals[3], dVals[4], dVals[5]);
                    }).collect(Collectors.toList());

            List<Vector3> coordinates = ffsLines.stream().map(line -> {
                double x = Calc.x_polarToCartesian(line);
                double y = Calc.y_polarToCartesian(line);
                double z = Calc.z_polarToCartesian(line);

                return new Vector3((float) (x * scalingFactor), (float) (y*scalingFactor) , (float) (z*scalingFactor));
            }).collect(Collectors.toList());

            return coordinates;
    }
}