package swati4star.createpdf.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;

import java.io.InputStream;

public abstract class FileReader {
    private static final String TAG = "FileReader";

    Context mContext;

    public FileReader(Context context) {
        mContext = context;
    }

    boolean read(Uri uri, Document document, Font myfont) throws Exception {
        InputStream inputStream = null;
        java.io.BufferedInputStream bufferedInputStream = null;
        try {
            inputStream = mContext.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                throw new IllegalStateException("Unable to read input stream for the provided Uri");
            }
            bufferedInputStream = new java.io.BufferedInputStream(inputStream);
            return createDocumentFromStream(uri, document, myfont, bufferedInputStream);
        } finally {
            try {
                if (bufferedInputStream != null)
                    bufferedInputStream.close();
            } catch (Exception e) {
                Log.w(TAG, "Error closing buffered stream", e);
            }
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                Log.w(TAG, "Error closing input stream", e);
            }
        }
    }

    protected abstract boolean createDocumentFromStream(
            Uri uri, Document document, Font myfont, InputStream inputStream) throws Exception;
}