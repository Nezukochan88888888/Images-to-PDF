package swati4star.createpdf.util;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

import swati4star.createpdf.R;
import swati4star.createpdf.interfaces.OnPDFCreatedInterface;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfWriter;

public class WordToPdfAsync extends AsyncTask<Void, Void, String> {

    private final Uri mWordUri;
    private final String mWordPath;
    private final String mOutputName;
    private final String mStorePath;
    private final Activity mActivity;
    private final OnPDFCreatedInterface mCallback;

    public WordToPdfAsync(Uri wordUri, String wordPath, String outputName, String storePath, Activity activity,
            OnPDFCreatedInterface callback) {
        this.mWordUri = wordUri;
        this.mWordPath = wordPath;
        this.mOutputName = outputName;
        this.mStorePath = storePath;
        this.mActivity = activity;
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mCallback.onPDFCreationStarted();
    }

    @Override
    protected String doInBackground(Void... voids) {
        String mPath = mStorePath + mOutputName + mActivity.getString(R.string.pdf_ext);
        Document document = new Document();
        boolean hasContent = false;
        File tempFile = null;
        try {
            PdfWriter.getInstance(document, new FileOutputStream(mPath));
            document.open();

            Font font = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL);
            FileReader reader;

            Uri wordUri = mWordUri;
            if (wordUri == null && mWordPath != null) {
                wordUri = Uri.fromFile(new File(mWordPath));
            }

            if (wordUri == null) {
                return null;
            }

            tempFile = copyToTempFile(wordUri);
            if (tempFile == null) {
                return null;
            }
            Uri tempUri = Uri.fromFile(tempFile);

            // Determine type from path or Uri
            String fileName = mWordPath;
            if (fileName == null) {
                fileName = new FileUtils(mActivity).getFileName(wordUri);
            }

            if (fileName == null) {
                return null;
            }

            String lowerFileName = fileName.toLowerCase(Locale.US);
            if (lowerFileName.endsWith(Constants.docExtension)) {
                try {
                    reader = new DocFileReader(mActivity);
                    hasContent = reader.read(tempUri, document, font);
                } catch (Throwable e) {
                    Log.w("WordToPdfAsync", "Doc parsing failed, attempting Fallback as Docx", e);
                    reader = new DocxFileReader(mActivity);
                    hasContent = reader.read(tempUri, document, font);
                }
            } else if (lowerFileName.endsWith(Constants.docxExtension)) {
                try {
                    reader = new DocxFileReader(mActivity);
                    hasContent = reader.read(tempUri, document, font);
                } catch (Throwable e) {
                    Log.w("WordToPdfAsync", "Docx parsing failed, attempting Fallback as Doc", e);
                    reader = new DocFileReader(mActivity);
                    hasContent = reader.read(tempUri, document, font);
                }
            } else {
                return null;
            }

            if (!hasContent) {
                return null;
            }
            return mPath;

        } catch (Throwable e) {
            Log.e("WordToPdfAsync", "Word to PDF conversion failed", e);
            e.printStackTrace();
            return null;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            if (document.isOpen()) {
                try {
                    document.close();
                } catch (Exception e) {
                    Log.e("WordToPdfAsync", "Error closing document", e);
                }
            }
            if (!hasContent) {
                File outputFile = new File(mPath);
                if (outputFile.exists()) {
                    outputFile.delete();
                }
            }
        }
    }

    @Override
    protected void onPostExecute(String resultPath) {
        super.onPostExecute(resultPath);
        if (resultPath != null) {
            mCallback.onPDFCreated(true, resultPath);
        } else {
            mCallback.onPDFCreated(false, null);
        }
    }

    private File copyToTempFile(Uri uri) {
        java.io.InputStream in = null;
        java.io.OutputStream out = null;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("word_to_pdf_", ".tmp", mActivity.getCacheDir());
            in = mActivity.getContentResolver().openInputStream(uri);
            if (in == null)
                return null;
            out = new FileOutputStream(tempFile);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
            return tempFile;
        } catch (Exception e) {
            Log.e("WordToPdfAsync", "Error copying to temp file", e);
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            return null;
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
                Log.w("WordToPdfAsync", "Error closing output", e);
            }
            try {
                if (in != null)
                    in.close();
            } catch (Exception e) {
                Log.w("WordToPdfAsync", "Error closing input", e);
            }
        }
    }
}
