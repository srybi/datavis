package de.th.ro.datavis.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;

public class FileProviderDatavis extends FileProvider {

    private final static String TAG = "FileProviderDatavis";
    private final static String AUTHORITY = "de.th.ro.datavis.util.provider"; // Has to be the same as in the Manifest


    /**
     * Provides the Uri to a File based on the given FileName
     * @param context context
     * @param fileName Name of the File
     * @return return Uri to a File with the given Filename, return null on Exeption
     */
    public static Uri getURIForAntenna(Context context, String fileName){

        File antennaFile = FileProviderDatavis.getAFileByFileName_FromDownloadDir(fileName);

        if (antennaFile == null){
            return null;
        }

        return FileProviderDatavis.getUriForFile(context, AUTHORITY, antennaFile);

    }

    /**
     * Provides the the File from the Download Dir based on the filename
     * @param fileName Name of the File
     * @return a File based on the filename
     */
    public static File getAFileByFileName_FromDownloadDir(String fileName) {

        File extStorage = Environment.getExternalStorageDirectory();
        File result = new File(extStorage.getAbsolutePath() +
                File.separator + "Download" +
                File.separator + fileName);
        if (!result.exists()) {
            Log.i(TAG, "File not found: : " + result.getAbsolutePath());
            result = null;
        }

        return result;
    }


}
