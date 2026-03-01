package swati4star.createpdf.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;

import java.io.InputStream;

public class DocFileReader extends FileReader {

    private static final String TAG = "DocFileReader";

    public DocFileReader(Context context) {
        super(context);
    }

    @Override
    protected boolean createDocumentFromStream(
            Uri uri, Document document, Font myfont, InputStream inputStream) throws Exception {
        HWPFDocument doc = new HWPFDocument(inputStream);
        WordExtractor extractor = new WordExtractor(doc);
        String[] paragraphs = extractor.getParagraphText();
        boolean hasContent = false;

        for (String paragraphTerm : paragraphs) {
            if (paragraphTerm != null && !paragraphTerm.trim().isEmpty()) {
                hasContent = true;
                Paragraph documentParagraph = new Paragraph(paragraphTerm, myfont);
                documentParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
                document.add(documentParagraph);
            }
        }
        return hasContent;
    }

}