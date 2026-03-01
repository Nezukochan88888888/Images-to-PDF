package swati4star.createpdf.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.InputStream;

public class DocxFileReader extends FileReader {

    private static final String TAG = "DocxFileReader";

    public DocxFileReader(Context context) {
        super(context);
    }

    @Override
    protected boolean createDocumentFromStream(
            Uri uri, Document document, Font myfont, InputStream inputStream) throws Exception {

        XWPFDocument doc = null;
        org.apache.poi.openxml4j.opc.OPCPackage pkg = null;
        try {
            if ("file".equals(uri.getScheme())) {
                pkg = org.apache.poi.openxml4j.opc.OPCPackage.open(uri.getPath());
                doc = new XWPFDocument(pkg);
            } else {
                doc = new XWPFDocument(inputStream);
            }
            boolean hasContent = false;
            for (XWPFParagraph paragraph : doc.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    hasContent = true;
                    Paragraph documentParagraph = new Paragraph(text + "\n", myfont);
                    documentParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    document.add(documentParagraph);
                }
            }
            return hasContent;
        } finally {
            try {
                if (pkg != null)
                    pkg.close();
            } catch (Exception e) {
                Log.w(TAG, "Error closing OPCPackage", e);
            }
        }
    }
}