package de.th.ro.datavis.util.worker;

import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;

public class WorkerRequestUtil {



    public static OneTimeWorkRequest getOneTimeRequest(Class<? extends ListenableWorker> worker , Data inputData){
        return new OneTimeWorkRequest.Builder(worker)
                .setInputData(inputData)
                .build();
    }

    public static OneTimeWorkRequest getOneTimeRequest(Class<? extends ListenableWorker> worker){
        return new OneTimeWorkRequest.Builder(worker)
                .build();
    }

}
