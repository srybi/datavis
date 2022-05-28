package de.th.ro.datavis.interpreter.ffs;

import android.content.Context;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.th.ro.datavis.db.daos.AtomicFieldDao;
import de.th.ro.datavis.db.database.AppDatabase;
import de.th.ro.datavis.models.AtomicField;
import de.th.ro.datavis.models.Result;
import de.th.ro.datavis.models.Sphere;
import de.th.ro.datavis.util.enums.InterpretationMode;
import de.th.ro.datavis.util.exceptions.FFSInterpretException;

public class FFSService {
    private final FFSInterpreter interpreter;
    private final Executor executor;
    private final AppDatabase db;

    public FFSService(FFSInterpreter interpreter, Context context) {
        executor = Executors.newSingleThreadExecutor();
        db = AppDatabase.getInstance(context);
        this.interpreter = interpreter;
    }

    void interpretData(InputStream stream, double scalingFactor, InterpretationMode mode) throws FFSInterpretException{
        Result<AtomicField> field = interpreter.interpretData(stream, scalingFactor, mode);
        saveSpheresIfNotExist(field.getData());
    }

    void interpretData(File file, double scalingFactor, InterpretationMode mode) throws FFSInterpretException{
        Result<AtomicField> field = interpreter.interpretData(file, scalingFactor, mode);
        saveSpheresIfNotExist(field.getData());
    }

    private void saveSpheresIfNotExist(AtomicField field) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                db.atomicFieldDao().insert(field);
            }
        });
    }
}
