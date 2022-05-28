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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import de.th.ro.datavis.models.Antenna;
import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.util.enums.MetadataType;
import de.th.ro.datavis.util.exceptions.CSVException;

public class MetadataInterpreter {

    private static final String LOG_TAG = "MetaIntrp";

    public MetadataInterpreter() {}

    /**
     *
     * @param in InputStream from file
     * @return  Matrix of .csv Data
     * @throws CSVException
     */
    public String[][] interpretCSV(InputStream in) throws IOException {
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(reader);
        Log.d(LOG_TAG, "Interpreting csv as matrix...");

        List<String[]> rowList = new ArrayList<>();
        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineItems = line.split(",");
                rowList.add(lineItems);
            }
        }
        catch(IOException e){}

        String[][] matrix = new String[rowList.size()][];
        for (int i = 0; i < rowList.size(); i++) {
            String[] row = rowList.get(i);
            matrix[i] = row;
        }
        Log.d(LOG_TAG, "Finished generating csv matrix...");
        return matrix;

    }

    /**
     *
     * @param in - Input Stream
     * @return Simple List of lines of the .csv Data
     */
    public List<String> interpretMetaData(InputStream in) throws IOException {
        InputStreamReader reader = new InputStreamReader(in);
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e){}

        return lines;
    }

    /**
     * @param matrix - Takes .csv matrix
     * @return - Gives out Metadata information, including tilt etc.
     * Excluding Filename, AntennaID
     */
    public List<MetaData> getMetadataFromLines(String[][] matrix) {
        List<MetaData> mList = new ArrayList<>();
        for(int i=1; i<matrix.length; i++) {
            for(int j=1; j<matrix[i].length; j++) {
                MetaData m = new MetaData(matrix[i][0],matrix[0][j],matrix[i][j]);
                mList.add(m);
            }
        }
        Log.d(LOG_TAG, "Finished making MetaData from matrix...");
        return mList;
    }

}
