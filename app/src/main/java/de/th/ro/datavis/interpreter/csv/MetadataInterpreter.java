package de.th.ro.datavis.interpreter.csv;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


import de.th.ro.datavis.models.MetaData;
import de.th.ro.datavis.util.constants.MetadataType;
import de.th.ro.datavis.util.filehandling.FileHandler;

public class MetadataInterpreter {

    private static final String LOG_TAG = "MetaIntrp";

    public MetadataInterpreter() {}

    /*
     * Checks if uris null and handles exceptions
     * Calls getMetadataFromLines() and interpretCSV()
     * Sets antenna Type based on filename
     */
    public List<MetaData> getCSVMetadata(Uri uri, ContentResolver c){
        List<MetaData> m = null;
        try {
            if(uri == null){
                Log.d(LOG_TAG, "File not found");
                m.add(new MetaData(0,0,"N/A"));
            }else{
                InputStream in = c.openInputStream(uri);
                m = getMetadataFromLines(interpretCSV(in));
                //Set Type
                String name = FileHandler.queryName(c, uri);
                if(name.contains(".")) name=name.substring(0, name.lastIndexOf('.'));
                for(MetaData mD :m){
                    mD.setType(name);
                }
            }
        } catch (IOException e) {
            Log.d(LOG_TAG,"Input Stream read failed: " + e.getMessage());
        } catch(SecurityException se){
            Log.d(LOG_TAG,"Permissions for Stream failed: " + se.getMessage());
        }
        return m;
    }

    /**
     * @param matrix - Takes a matrix generated from .csv
     * @return - returns Metadata information, including tilt etc.
     * Excluding Filename, AntennaID
     */
    public List<MetaData> getMetadataFromLines(String[][] matrix) {
        List<MetaData> mList = new ArrayList<>();
        for(int i=1; i<matrix.length; i++) {
            for(int j=1; j<matrix[i].length; j++) {
                try {
                    MetaData m = new MetaData(Double.parseDouble(matrix[i][0]),Double.parseDouble(matrix[0][j]),matrix[i][j]);
                    mList.add(m);
                } catch (Exception e){
                    Log.d(LOG_TAG,"Empty string found in .csv: "+ i+j);
                }
            }
        }
        Log.d(LOG_TAG, "Finished making MetaData from .csv matrix with "+ mList.size() + " entries");
        return mList;
    }

    /**
     *
     * @param in InputStream from file
     * @return  Matrix of .csv Data
     */
    public String[][] interpretCSV(InputStream in) throws IOException {
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
        catch(IOException e){}

        String[][] matrix = new String[rowList.size()][];
        for (int i = 0; i < rowList.size(); i++) {
            String[] row = rowList.get(i);
            matrix[i] = row;
        }
        return matrix;
    }

    /**
     * Obsolete
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
    /*
     * Utility to check MIME type
     */
    private static boolean isCSV(String mimeType) {
        return "text/comma-separated-values".equals(mimeType);
    }
    private static boolean isMetaDataImportable(String type) {
        return(MetadataType.MetaDataTypeList.contains(type));
    }

}
