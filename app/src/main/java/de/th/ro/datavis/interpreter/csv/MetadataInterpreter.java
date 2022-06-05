package de.th.ro.datavis.interpreter.csv;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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

    public List<MetaData> getCSVMetadata(Intent data, ContentResolver c){
        List<MetaData> m = null;

        try {
            if(data.getData() == null){
                m.add(new MetaData(0,0,"N/A"));
            }else{
                InputStream in = c.openInputStream(data.getData());
                Log.d(LOG_TAG, "Input Stream open");
                m = getMetadataFromLines(interpretCSV(in));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch(SecurityException se){
            se.printStackTrace();
            //Toast.makeText(this, "Unable to load the file, due to missing permissions.", Toast.LENGTH_SHORT).show();
            return null;
        }
        return m;
    }
    public List<MetaData> getCSVMetadata(Uri uri, ContentResolver c){
        List<MetaData> m = null;

        try {
            if(uri == null){
                Log.d(LOG_TAG, "File not found");
                m.add(new MetaData(0,0,"N/A"));
            }else{

                InputStream in = c.openInputStream(uri);
                //getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.d(LOG_TAG, "Input Stream open");
                m = getMetadataFromLines(interpretCSV(in));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch(SecurityException se){
            se.printStackTrace();
            //Toast.makeText(this, "Unable to load the file, due to missing permissions.", Toast.LENGTH_SHORT).show();
            return null;
        }
        return m;
    }

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
                MetaData m = new MetaData(Double.parseDouble(matrix[i][0]),Integer.parseInt(matrix[0][j]),matrix[i][j]);
                mList.add(m);
            }
        }
        Log.d(LOG_TAG, "Finished making MetaData from matrix...");
        return mList;
    }

}
