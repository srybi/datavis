package de.th.ro.datavis.util.worker;

import androidx.work.WorkInfo;

public class WorkerStateUtil {



    /**
     * @param state WorkInfo state
     * @param expectedState WorkInfo state
     * @return compare both States
     */
    public static boolean checkWorkInfoState(WorkInfo.State state, WorkInfo.State expectedState ){
        return state.compareTo(expectedState) == 0;
    }

}
