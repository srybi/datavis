package de.th.ro.datavis.interpreter.ffs;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.lifecycle.LiveData;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.th.ro.datavis.db.daos.AtomicFieldDao;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.interfaces.IInterpreter;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.Result;
import de.th.ro.datavis.models.Sphere;
import de.th.ro.datavis.util.enums.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class FFSService {
    private final String TAG = "FFSService";

    private final IInterpreter interpreter;
    private final ExecutorService executor;
    private final AppDatabase db;

    private AtomicField atomicField;
    private List<Double> frequencies;

    public FFSService(IInterpreter interpreter, Context context) {
        executor = Executors.newSingleThreadExecutor();
        db = AppDatabase.getInstance(context);
        this.interpreter = interpreter;
    }

    public void interpretData(InputStream stream, double scalingFactor, int antennaId) throws FFSInterpretException{

        Result<Pair<ArrayList<AtomicField>, ArrayList<AtomicField>>> fields = interpreter.interpretData(stream, scalingFactor, antennaId);
        if(fields.isSuccess()){
            Pair<ArrayList<AtomicField>, ArrayList<AtomicField>> pair = fields.getData();
            saveSpheresIfNotExist(pair.first, pair.second);
        }

    }

    public void interpretData(File file, double scalingFactor, int antennaId) throws FFSInterpretException{
        Result<Pair<ArrayList<AtomicField>, ArrayList<AtomicField>>> fields = interpreter.interpretData(file, scalingFactor, antennaId);
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
                        e.printStackTrace();
                        Log.d("FFSService", "SQL Error: " + e.getMessage());
                    }
                }
                //Save all linear spheres
                for (AtomicField field : fieldsLin) {
                    try {
                        db.atomicFieldDao().insert(field);
                    }catch (SQLiteConstraintException e){
                        e.printStackTrace();
                        Log.d("FFSService", "SQL Error: " + e.getMessage());
                    }
                }
            }


    public AtomicField getSpheresByPrimaryKey(int antennaId, double frequency, int tilt, InterpretationMode mode) {

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

    public List<Double> FrequenciesForAntenna(int antennaId, int tilt, InterpretationMode mode) {

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
