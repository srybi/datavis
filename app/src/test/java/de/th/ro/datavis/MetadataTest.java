package de.th.ro.datavis;

import android.util.Log;

import org.junit.Test;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.th.ro.datavis.interpreter.csv.MetadataInterpreter;
import de.th.ro.datavis.util.exceptions.CSVException;

public class MetadataTest {

    private static String path = "C:\\Users\\Tassilo\\Documents\\datavis\\Synth_Array_Data\\Theta_max.csv";
    private static String csv = "NaN,2,8,14\n"+
            "1.7,59.976,59.787,59.339\n"+
            "1.8,59.975,59.787,59.339\n"+
            "1.9,59.974,59.787,59.338\n"+
            "2,59.973,59.787,59.337\n"+
            "2.1,59.972,59.787,59.337\n"+
            "2.2,59.971,59.787,59.336\n"+
            "2.3,59.97,59.787,59.335\n"+
            "2.4,59.969,59.787,59.335\n"+
            "2.5,59.968,59.787,59.334\n"+
            "2.6,59.966,59.787,59.333\n"+
            "2.7,59.965,59.787,59.332";


    @Test
    public void readLineTest() {
        MetadataInterpreter m = new MetadataInterpreter();
        try (InputStream inputCSV = new ByteArrayInputStream(csv.getBytes()))
        {
            List<String> s = m.interpretMetaData(inputCSV);
            Assert.assertEquals(11,s.size());
            Assert.assertEquals("1.7,59.976,59.787,59.339",s.get(0));
        } catch (IOException e) {}
    }

    @Test
    public void readMatrixTest() throws Exception {
        MetadataInterpreter m = new MetadataInterpreter();
        try (InputStream inputCSV = new ByteArrayInputStream(csv.getBytes()))
        {
            String[][] s = m.interpretCSV(inputCSV);
            Assert.assertEquals("2",s[4][0]);
            Assert.assertEquals("59.339",s[2][3]);
            System.out.println(s[1].length);
            System.out.println(s.length);
        } catch (Exception e) {}
    }
    @Test
    public void iterateMatrixTest() throws CSVException, FileNotFoundException {
        MetadataInterpreter m = new MetadataInterpreter();
        try (InputStream inputCSV = new ByteArrayInputStream(csv.getBytes());)
        {
            String[][] s = m.interpretCSV(inputCSV);
            for(int i=1; i<s.length; i++) {
                for(int j=1; j<s[i].length; j++) {
                    System.out.println("Freq: " + s[i][0]+ " Tilt: " +s[0][j]+ " Value: " +s[i][j]);
                }
            }
        } catch (Exception e) {}
    }

    @Test
    public void readCSVTest(){
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(records.get(0));
    }

}
