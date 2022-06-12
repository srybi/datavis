package de.th.ro.datavis.util.filehandling;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.room.util.StringUtil;

import de.th.ro.datavis.util.constants.FileRequests;

public class FileHandler {
    public static final String TAG = "FileHandler";

    public static String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    /**
     * Takes resolver, URI, requestCode
     * Depending on requestCode checks against file Extension or Mimes
     *      Metadatafolder checks in #MetadataInterpreter
     */
    public static boolean fileCheck(ContentResolver resolver, Uri uri, int requestCode){
        switch (requestCode) {
            case FileRequests.REQUEST_CODE_ANTENNA:
                return checkExtension(resolver, uri, new String[]{".glb", ".gltf"});
            case FileRequests.REQUEST_CODE_FFS:
                return checkExtension(resolver, uri, new String[]{".ffs"});
            case FileRequests.REQUEST_CODE_METADATA:
                return checkMime(resolver, uri, new String[]{"text/comma-separated-values", "text/csv"});
            case FileRequests.REQUEST_CODE_METADATAFOLDER:
                return true;
            default: throw new RuntimeException();
        }
    }

    /** Compares acceptable MIMEs and compares to the Actual **/
    private static boolean checkMime (ContentResolver resolver, Uri uri, String[] mimesAccepted){
        String mimeActual = queryMime(resolver, uri);
        Log.d(TAG, "Mime found: "+mimeActual+"| Mimes accepted: " + TextUtils.join(", ", mimesAccepted));
        for(String m :mimesAccepted){
            if(mimeActual.equals(m)) return true;
        }
        Log.d(TAG, "RETURN FALSE");
        return false;
    }

    /** Compares acceptable Extension and compares to the Actual **/
    private static boolean checkExtension (ContentResolver resolver, Uri uri, String[] extAccepted){
        String extActual = queryFileExtension(resolver, uri);
        Log.d(TAG, "Extension found: "+extActual+" | Extension accepted: " + TextUtils.join(", ", extAccepted));
        for(String m :extAccepted){
            if(extActual.equals(m)) return true;
        }
        return false;
    }

    /** Returns MIME Type **/
    public static String queryMime(ContentResolver resolver, Uri uri){
        Cursor c =
                resolver.query(uri,new String[]{
                                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                                DocumentsContract.Document.COLUMN_MIME_TYPE},
                        null, null, null);
        assert c != null;
        c.moveToFirst();
        String mime = c.getString(2);
        Log.d(TAG, "Found file: "+c.getString(0)+" " +c.getString(1)+" " +c.getString(2));
        c.close();
        return mime;
    }

    /** Returns File Extension **/
    public static String queryFileExtension(ContentResolver resolver, Uri uri){
        Cursor c =
                resolver.query(uri,new String[]{
                                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                                DocumentsContract.Document.COLUMN_MIME_TYPE},
                        null, null, null);
        assert c != null;
        c.moveToFirst();
        String ext = c.getString(1);
        if(ext.contains(".")) ext=ext.substring(ext.lastIndexOf('.'));
        Log.d(TAG, "Found file: "+c.getString(0)+" " +c.getString(1)+" " +c.getString(2));
        c.close();
        return ext;
    }

}
