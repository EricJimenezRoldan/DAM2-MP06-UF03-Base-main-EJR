package cat.iesesteveterradas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

public class ProcessQueries {
    private static final String INPUT_DIR = "./data/input/";
    private static final String OUTPUT_DIR = "./data/output/";
    private BaseXConnection connection;

    public ProcessQueries(BaseXConnection connection) {
        this.connection = connection;
    }

    public void processQueries() {
        try (Stream<Path> filePathStream = Files.walk(Paths.get(INPUT_DIR))) { // Entra al directori
            filePathStream.filter(Files::isRegularFile) // Comprova que es un arxiu
                          .forEach(this::executeAndSaveQuery);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeAndSaveQuery(Path filePath) {
        try {
            String query = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            String result = connection.executeQuery(query);
            saveResult(filePath.getFileName().toString().replace(".xquery", ".xml"), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveResult(String fileName, String result) {
        try {
            Path outputPath = Paths.get(OUTPUT_DIR, fileName);
            Files.createDirectories(outputPath.getParent()); 
            Files.write(outputPath, result.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            BaseXConnection connection = new BaseXConnection("localhost", 1984, "admin", "admin");
            ProcessQueries processor = new ProcessQueries(connection);
            processor.processQueries();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
