package swati4star.createpdf.util;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileOutputStream;

import swati4star.createpdf.R;
import swati4star.createpdf.interfaces.OnPDFCreatedInterface;

public class PdfToDocxAsync extends AsyncTask<Void, Void, String> {

    private static final String TAG = "PdfToDocxAsync";

    private final String mPdfPath;
    private final String mOutputName;
    private final String mStorePath;
    private final Activity mActivity;
    private final OnPDFCreatedInterface mCallback;

    public PdfToDocxAsync(String pdfPath, String outputName, String storePath, Activity activity,
            OnPDFCreatedInterface callback) {
        this.mPdfPath = pdfPath;
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
        PdfReader reader = null;
        XWPFDocument document = null;
        FileOutputStream out = null;
        String mPath = null;
        boolean success = false;

        try {
            if (mPdfPath == null || mPdfPath.isEmpty()) {
                Log.e(TAG, "PDF path is null or empty");
                return null;
            }

            reader = new PdfReader(mPdfPath);
            int n = reader.getNumberOfPages();

            if (n == 0) {
                Log.w(TAG, "PDF has no pages");
                return null;
            }

            document = new XWPFDocument();
            boolean hasText = false;

            for (int i = 0; i < n; i++) {
                try {
                    String pageText = PdfTextExtractor.getTextFromPage(reader, i + 1);
                    if (pageText != null && !pageText.trim().isEmpty()) {
                        hasText = true;
                        String[] lines = pageText.split("\\r?\\n");
                        for (String line : lines) {
                            XWPFParagraph paragraph = document.createParagraph();
                            XWPFRun run = paragraph.createRun();
                            run.setText(line);
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error extracting text from page " + (i + 1) + ", skipping", e);
                }
            }

            if (!hasText) {
                Log.w(TAG, "No text found in PDF");
                return null;
            }

            mPath = mStorePath + mOutputName + mActivity.getString(R.string.docx_ext);
            File docxFile = new File(mPath);
            out = new FileOutputStream(docxFile);
            document.write(out);
            out.flush();
            success = true;

            Log.i(TAG, "DOCX created successfully: " + mPath);
            return mPath;

        } catch (Exception e) {
            Log.e(TAG, "PDF to DOCX conversion failed", e);
            return null;
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing output stream", e);
            }
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing PdfReader", e);
            }
            if (!success && mPath != null) {
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
}
