package cat.iesesteveterradas;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import org.bson.Document;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PR32QueryMain {
    public static void main(String[] args) {
        // Conexión a la base de datos de MongoDB
        MongoClient mongoClient = MongoClients.create("mongodb://root:example@localhost:27017/?authSource=admin");
        MongoDatabase database = mongoClient.getDatabase("PR132");
        MongoCollection<Document> collection = database.getCollection("questions");

        // Calcular la media de ViewCount en toda la colección
        double averageViewCount = calculateAverageViewCount(collection);

        // Obtener los documentos cuyo ViewCount es mayor que la media
        List<Document> questionsAboveAverage = getDocumentsAboveAverageViewCount(collection, averageViewCount);

        // Consulta 2: Obtener las preguntas que contienen ciertas letras en el título
        List<String> lettersToSearch = Arrays.asList("pug", "wig", "yak", "nap", "jig", "mug", "zap", "gag", "oaf", "elf");
        List<String> questionsWithLetters = getQuestionsWithTitleContainingLetters(collection, lettersToSearch);

        // Generar informe PDF
        generatePDFReport("Informe1.pdf", questionsAboveAverage);
        generatePDFReportForStrings("Informe2.pdf", questionsWithLetters);

        // Cerrar la conexión a MongoDB
        mongoClient.close();
    }

    private static double calculateAverageViewCount(MongoCollection<Document> collection) {
        FindIterable<Document> documents = collection.find();
        int totalCount = 0;
        int count = 0;
        for (Document doc : documents) {
            String viewCountStr = doc.getString("ViewCount");
            if (viewCountStr != null && viewCountStr.matches("\\d+")) {
                totalCount += Integer.parseInt(viewCountStr);
                count++;
            }
        }
        return count > 0 ? (double) totalCount / count : 0;
    }

    private static List<Document> getDocumentsAboveAverageViewCount(MongoCollection<Document> collection, double averageViewCount) {
        List<Document> documentsAboveAverage = new ArrayList<>();
        FindIterable<Document> documents = collection.find();
        for (Document doc : documents) {
            String viewCountStr = doc.getString("ViewCount");
            if (viewCountStr != null && viewCountStr.matches("\\d+")) {
                int viewCount = Integer.parseInt(viewCountStr);
                if (viewCount > averageViewCount) {
                    documentsAboveAverage.add(doc);
                }
            }
        }
        return documentsAboveAverage;
    }

    private static List<String> getQuestionsWithTitleContainingLetters(MongoCollection<Document> collection, List<String> lettersToSearch) {
        List<Document> regexQueries = lettersToSearch.stream()
                .map(letter -> new Document("Title", new Document("$regex", letter)))
                .collect(Collectors.toList());

        FindIterable<Document> documents = collection.find(new Document("$or", regexQueries));
        List<String> questions = new ArrayList<>();
        for (Document doc : documents) {
            questions.add(doc.getString("Title"));
        }
        return questions;
    }

    private static void generatePDFReport(String filename, List<Document> documents) {
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

            float yStart = page.getMediaBox().getHeight() - 50;
            float margin = 50;
            float width = page.getMediaBox().getWidth() - 2 * margin;

            for (Document doc : documents) {
                String questionTitle = doc.getString("Title");
                if (yStart < 50) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    yStart = page.getMediaBox().getHeight() - 50;
                }
                List<String> lines = splitText(questionTitle, width);
                for (String line : lines) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yStart);
                    contentStream.showText(line);
                    contentStream.endText();
                    yStart -= 15;
                }
            }

            contentStream.close();

            document.save("./data/output/" + filename);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generatePDFReportForStrings(String filename, List<String> questions) {
        try {
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);

            float yStart = page.getMediaBox().getHeight() - 50;
            float margin = 50;
            float width = page.getMediaBox().getWidth() - 2 * margin;

            for (String question : questions) {
                if (yStart < 50) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    yStart = page.getMediaBox().getHeight() - 50;
                }
                List<String> lines = splitText(question, width);
                for (String line : lines) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yStart);
                    contentStream.showText(line);
                    contentStream.endText();
                    yStart -= 15;
                }
            }

            contentStream.close();

            document.save("./data/output/" + filename);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> splitText(String text, float width) throws IOException {
        List<String> lines = new ArrayList<>();
        int lastSpace = -1;
        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0)
                spaceIndex = text.length();
            String subString = text.substring(0, spaceIndex);
            float size = PDType1Font.HELVETICA_BOLD.getStringWidth(subString) / 1000 * 12;
            if (size > width) {
                if (lastSpace < 0)
                    lastSpace = spaceIndex;
                subString = text.substring(0, lastSpace);
                lines.add(subString);
                text = text.substring(lastSpace).trim();
                lastSpace = -1;
            } else if (spaceIndex == text.length()) {
                lines.add(text);
                text = "";
            } else {
                lastSpace = spaceIndex;
            }
        }
        return lines;
    }
}
