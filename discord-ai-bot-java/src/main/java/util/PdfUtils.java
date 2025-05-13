package util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PdfUtils {

    /**
     * Extrae texto de un archivo PDF usando Apache PDFBox.
     *
     * @param file El archivo PDF del que se desea extraer texto.
     * @return Texto extraído del PDF.
     * @throws IOException si ocurre un error al leer el PDF.
     */
    public static String extractText(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
