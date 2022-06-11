package de.th.ro.datavis.interpreter.ffs;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interfaces.IInterpreter;
import de.th.ro.datavis.models.AntennaField;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.Result;
import de.th.ro.datavis.util.enums.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class FFSService {
    private final IInterpreter interpreter;
    private final ExecutorService executor;
    private final AppDatabase db;

    private AtomicField atomicField;
    private List<Double> frequencies;
    private List<Double> tilts;
    private double tilt;

    public FFSService(IInterpreter interpreter, Context context) {
        executor = Executors.newSingleThreadExecutor();
        db = AppDatabase.getInstance(context);
        this.interpreter = interpreter;
    }

    public void interpretData(InputStream stream, double scalingFactor, int antennaId, String filename) throws FFSInterpretException{
        double floatingPoint = 1;
        int start = filename.indexOf("_T") + 2, end = start + 2;
        if (filename.charAt(start+1)=='.') {
            end = filename.indexOf("e+");
            floatingPoint = Math.pow(10, Double.parseDouble(filename.substring(end+2, end+4)));
        }
        Result<Pair<ArrayList<AtomicField>, ArrayList<AtomicField>>> fields = interpreter.interpretData(stream, scalingFactor, Double.parseDouble(filename.substring(start, end))*floatingPoint, antennaId);
        if(fields.isSuccess()){
            Pair<ArrayList<AtomicField>, ArrayList<AtomicField>> pair = fields.getData();
            saveSpheresIfNotExist(pair.first, pair.second);
        }

    }

    public void interpretData(File file, double scalingFactor, int antennaId) throws FFSInterpretException{
        double floatingPiont = 1;
        String filename = file.getName();
        int start = filename.indexOf("_T") + 2, end = start + 2;
        if (filename.charAt(start+1)=='.') {
            end = filename.indexOf("e+");
            floatingPiont = Math.pow(10, Double.parseDouble(filename.substring(end+2, end+4)));
        }
        Result<Pair<ArrayList<AtomicField>, ArrayList<AtomicField>>> fields = interpreter.interpretData(file, scalingFactor, Double.parseDouble(filename.substring(start, end))*floatingPiont, antennaId);
        if(fields.isSuccess()){
            Pair<ArrayList<AtomicField>, ArrayList<AtomicField>> pair = fields.getData();
            saveSpheresIfNotExist(pair.first, pair.second);
        }
    }

    private void saveSpheresIfNotExist(ArrayList<AtomicField> fieldsLog, ArrayList<AtomicField> fieldsLin) {

                //Save all logarithmic spheres
                for (AtomicField field : fieldsLog) {
                    try {
                        db.atomicFieldDao().insert(field);
                    }catch (SQLiteConstraintException e){
                        Log.d("FFSService", "Field already exists");
                    }
                }
                //Save all linear spheres
                for (AtomicField field : fieldsLin) {
                    try {
                        db.atomicFieldDao().insert(field);
                    }catch (SQLiteConstraintException e){
                        Log.d("FFSService", "Field already exists");
                    }
                }
            }


    public AtomicField getSpheresByPrimaryKey(int antennaId, double frequency, double tilt, InterpretationMode mode) {

        Future future = executor.submit(new Runnable(){
            @Override
            public void run() {
                int modeInt = mode.ordinal();
                atomicField = db.atomicFieldDao().getAtomicFields_Background(antennaId, tilt, frequency, modeInt);
            }
        });

        try {
            future.get();
            return atomicField;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Double> FrequenciesForAntenna(int antennaId, double tilt, InterpretationMode mode) {

        Future future = executor.submit(new Runnable(){
            @Override
            public void run() {
                int modeInt = mode.ordinal();
                frequencies = db.atomicFieldDao().getFrequencies_Background(antennaId, tilt, modeInt);
            }
        });

        try {
            future.get();
            return frequencies;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Double> TiltsForAntenna(int antennaId, double frequency, InterpretationMode mode) {

        Future future = executor.submit(new Runnable(){
            @Override
            public void run() {
                int modeInt = mode.ordinal();
                tilts = db.atomicFieldDao().getTilts_Background(antennaId, frequency, modeInt);
            }
        });

        try {
            future.get();
            return tilts;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public double TiltsForAntenna(int antennaID) {
        Future future = executor.submit(new Runnable(){
            @Override
            public void run() {
                tilt = db.atomicFieldDao().getAtomicFieldsByAntennaFieldIdSync(antennaID).get(0).tilt;
            }
        });

        try {
            future.get();
            return tilt;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return tilt;
    }

    public FFSIntensityColor mapToColor(double intensity, double maxItensity) {
        //wie in CST
        double minIntensity = maxItensity - 1;
        double stepSize = (maxItensity - Math.abs(minIntensity))/6;

        if(intensity > maxItensity - (stepSize * 1)){
            //#FE0000 red
            return FFSIntensityColor.RED;
        }
        if(intensity > maxItensity - stepSize * 2){
            //#e6793a orange
            return FFSIntensityColor.ORANGE;
        }
        if(intensity > maxItensity - stepSize * 3){
            //#FFF205 yellow
            return FFSIntensityColor.YELLOW;
        }
        if(intensity > maxItensity - stepSize * 4){
            //#7CFF01 green
            return FFSIntensityColor.GREEN;
        }
        if(intensity > maxItensity - stepSize * 5){
            //#3befe5 baby blue
            return FFSIntensityColor.BABYBLUE;
        }
        //#01FFF4  blue
        return FFSIntensityColor.BLUE;
    }

}
