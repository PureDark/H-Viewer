package ml.puredark.hviewer.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.SyncStateContract;


/**
 * Created by GKF on 2016/12/10.
 */

public class GetContentUri {

    public Uri GetContentUriByParentUri(Context context, Uri parentUri, String dirname) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(parentUri,
                DocumentsContract.getTreeDocumentId(parentUri));
        Cursor childCursor = contentResolver.query(childrenUri,
                new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_DOCUMENT_ID},
                null,null,null);
        try {
            while (childCursor.moveToNext()) {
                String docname = childCursor.getString(0);
                String docid = childCursor.getString(2);
                if (docname.equals(dirname)) {
                    closeQuietly(childCursor);
                    Uri uri = DocumentsContract.buildDocumentUriUsingTree(parentUri,docid);
                    try {
                        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    return uri.normalizeScheme();
                }
            }
        } finally {
            closeQuietly(childCursor);
        }

        return null;
    }

    public void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }


}
