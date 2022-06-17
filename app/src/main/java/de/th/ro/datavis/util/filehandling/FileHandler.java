package de.th.ro.datavis.util.filehandling;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.th.ro.datavis.util.constants.FileRequests;
import de.th.ro.datavis.util.constants.MetadataType;

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
            case FileRequests.REQUEST_CODE_FOLDER:
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

    /**
     * Takes the directory URI, iterates through the files and builds two lists:
     * One of all .csv, one of all .ffs data
     */

    public static Map<Integer, String[]> traverseDirectoryEntries(Uri rootUri, ContentResolver cr) {

        Map<Integer, String[]> pairURI = new HashMap<>();
        ArrayList<Uri> listCSV = new ArrayList<>();
        ArrayList<Uri> listFFS = new ArrayList<>();


        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(rootUri,
                DocumentsContract.getTreeDocumentId(rootUri));

        // Keep track of our directory hierarchy
        List<Uri> dirNodes = new LinkedList<>();
        dirNodes.add(childrenUri);

        while(!dirNodes.isEmpty()) {
            childrenUri = dirNodes.remove(0); // get the item from top
            Log.d(TAG, "node uri: " + childrenUri);
            Cursor c = cr.query(childrenUri, new String[]{
                            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                            DocumentsContract.Document.COLUMN_MIME_TYPE},
                    null, null, null);
            try {
                while (c.moveToNext()) {
                    final String docId = c.getString(0);
                    String name = c.getString(1);
                    if(name.contains(".")) name=name.substring(0, name.lastIndexOf('.'));
                    final String mime = c.getString(2);
                    //Log.d(TAG, "docId: " + docId + ", name: " + name + ", mime: " + mime);
                    if (getFileExtension(docId).equals(".csv")
                            && isMetaDataImportable(name)) {
                        final Uri newNode = DocumentsContract.buildDocumentUriUsingTree(rootUri, docId);
                        listCSV.add(newNode);
                    } else if (getFileExtension(docId).equals(".ffs")){
                        final Uri newNode = DocumentsContract.buildDocumentUriUsingTree(rootUri, docId);
                        listFFS.add(newNode);
                    }
                }

            } finally {
                try {
                    c.close();
                } catch(RuntimeException re) {
                    re.printStackTrace();
                    Log.d(TAG, "cursor in directory did not close");
                }
            }
        }
        Log.d(TAG, "gathered URIs");

        pairURI.put(0, listCSV.stream().map(x->x.toString()).toArray(String[]::new));
        pairURI.put(1, listFFS.stream().map(x->x.toString()).toArray(String[]::new));

        Log.d(TAG, "Number of csv in directory: "+pairURI.get(0).length);
        Log.d(TAG, "Number of ffs in directory: "+pairURI.get(1).length);
        return pairURI;
    }

    private static String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return filename.substring(lastIndexOf);
    }

    /*
     * Only read Metadata that we want
     */
    private static boolean isMetaDataImportable(String type) {
        return(MetadataType.MetaDataTypeList.contains(type));
    }

}
