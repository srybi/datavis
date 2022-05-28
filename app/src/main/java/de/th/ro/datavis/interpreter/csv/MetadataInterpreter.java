package de.th.ro.datavis.interpreter.csv;

import android.net.Uri;
import android.util.Log;

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

import de.th.ro.datavis.interpreter.calc.Calc;
import de.th.ro.datavis.interpreter.ffs.FFSConstants;
import de.th.ro.datavis.models.Sphere;
import de.th.ro.datavis.util.enums.InterpretationMode;
import de.th.ro.datavis.util.enums.MetadataType;
import de.th.ro.datavis.util.exceptions.CSVException;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class MetadataInterpreter {

    public MetadataInterpreter() {}

    public String[][] interpretCSV(InputStream in) throws CSVException {
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(reader);

        List<String[]> rowList = new ArrayList<>();
        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineItems = line.split(",");
                rowList.add(lineItems);
            }
        }
        catch(IOException e){
            throw new CSVException("Read CSV failed");
        }

        String[][] matrix = new String[rowList.size()][];
        for (int i = 0; i < rowList.size(); i++) {
            String[] row = rowList.get(i);
            matrix[i] = row;
        }
        return matrix;
    }

}
