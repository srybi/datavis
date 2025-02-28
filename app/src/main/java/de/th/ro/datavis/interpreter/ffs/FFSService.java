package de.th.ro.datavis.interpreter.ffs;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.th.ro.datavis.database.AppDatabase;
import de.th.ro.datavis.interpreter.IInterpreter;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.Result;
import de.th.ro.datavis.util.constants.FFSIntensityColor;
import de.th.ro.datavis.util.constants.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class FFSService {
    private final String TAG = "FFSService";

    private final IInterpreter interpreter;
    private final ExecutorService executor;
    private final AppDatabase db;

    private AtomicField atomicField;
    private List<Double> frequencies;
    private List<Double> tilts;

    private Context context;

    public FFSService(IInterpreter interpreter, Context context) {
        executor = Executors.newSingleThreadExecutor();
        db = AppDatabase.getInstance(context);
        this.interpreter = interpreter;
        this.context = context;
    }

    public void interpretData(InputStream stream, double scalingFactor, int antennaId, String filename) throws FFSInterpretException{
        int start = filename.indexOf("_T") + 2;
        int end = start + 2;
        Result<Pair<ArrayList<AtomicField>, ArrayList<AtomicField>>> fields = null;
         if (filename.charAt(start+1)=='.') {
            end = filename.indexOf("0e") + 5;
        }
         if (start == 1)
            fields = interpreter.interpretData(stream, scalingFactor, 0, antennaId);
        else {
            try {
                 Double.parseDouble(filename.substring(start, end));
             } catch (NumberFormatException e)
             {
                 fields = Result.error(e.getMessage());
             }
         }
        if (fields == null)
            fields = interpreter.interpretData(stream, scalingFactor, Double.parseDouble(filename.substring(start, end)), antennaId);
        // Kein Tilt im Dateinamen

        if(fields.isSuccess()){
            Pair<ArrayList<AtomicField>, ArrayList<AtomicField>> pair = fields.getData();
            saveSpheresIfNotExist(pair.first, pair.second);
        }else{
            throw new FFSInterpretException(fields.getMessage());
        }
    }

    private void saveSpheresIfNotExist(ArrayList<AtomicField> fieldsLog, ArrayList<AtomicField> fieldsLin) {
        boolean success = true;
        //Save all logarithmic spheres
        for (AtomicField field : fieldsLog) {
            try {
                db.atomicFieldDao().insert(field);
            }catch (SQLiteConstraintException e){
                e.printStackTrace();
                Log.d("FFSService", "SQL Error: " + e.getMessage());
                success = false;
            }
        }
        //Save all linear spheres
        for (AtomicField field : fieldsLin) {
            try {
                db.atomicFieldDao().insert(field);
            }catch (SQLiteConstraintException e){
                e.printStackTrace();
                Log.d("FFSService", "SQL Error: " + e.getMessage());
                success = false;
            }
        }
        if(!success)
            Toast.makeText(context, "Error saving data", Toast.LENGTH_SHORT).show();
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

    public List<Double> FrequenciesForAntenna(int antennaId) {

        Future future = executor.submit(new Runnable(){
            @Override
            public void run() {
                frequencies = db.atomicFieldDao().getFrequenciesForAntenna_Background(antennaId);
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

    public List<Double> TiltsForAntenna(int antennaId) {

        Future future = executor.submit(new Runnable(){
            @Override
            public void run() {
                tilts = db.atomicFieldDao().getTiltsForAntenna_Background(antennaId);
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

    public FFSIntensityColor mapToColor(double intensity, double maxItensity) {
        //Schon wieder eine übergangslösung. Wenn Interpretation mode = Linear, dann minIntensity = 0, sonst werden die negativen Intensitäten sowieso rausgeschmissen
        //Muss überarbeitet werden sobald bei Log. Darstellung negative Intensitäten mitberücksichtig werden
        double stepSize = (maxItensity) / 6;
        Log.d(TAG, "mapToColor: " + intensity);

        if (intensity < maxItensity - (stepSize * 5)) {
            //#01FFF4  blue
            return FFSIntensityColor.BLUE;
        }
        if (intensity < maxItensity - (stepSize * 4)) {
            //#3befe5 baby blue
            return FFSIntensityColor.BABYBLUE;
        }
        if (intensity < maxItensity - (stepSize * 3)) {
            //#7CFF01 green
            return FFSIntensityColor.GREEN;
        }
        if (intensity < maxItensity - (stepSize * 2)) {
            //#FFF205 yellow
            return FFSIntensityColor.YELLOW;
        }
        if (intensity < maxItensity - (stepSize * 1)) {
            //#e6793a orange
            return FFSIntensityColor.ORANGE;
        }
        return FFSIntensityColor.RED;
    }

}
