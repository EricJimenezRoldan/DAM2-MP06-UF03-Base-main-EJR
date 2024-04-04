package cat.iesesteveterradas;

import cat.iesesteveterradas.BaseXConnection;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class Exercici3 {
    public static void main(String[] args) {
        String basePath = System.getProperty("user.dir") + "/data/models/";

        Set<String> uniqueEntities = new HashSet<>();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./data/noms_propis.txt"))) {
            // Carrega els models d'OpenNLP
            InputStream modelInSentence = new FileInputStream(basePath + "opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin");
            InputStream modelInToken = new FileInputStream(basePath + "opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin");
            InputStream modelInPerson = new FileInputStream(basePath + "en-ner-person.bin");

            SentenceModel sentenceModel = new SentenceModel(modelInSentence);
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);

            TokenizerModel tokenizerModel = new TokenizerModel(modelInToken);
            TokenizerME tokenizer = new TokenizerME(tokenizerModel);

            TokenNameFinderModel personModel = new TokenNameFinderModel(modelInPerson);
            NameFinderME nameFinder = new NameFinderME(personModel);

            // Connexió a BaseX
            BaseXConnection baseXConn = new BaseXConnection("localhost", 1984, "admin", "admin");

            // La teva consulta XQuery aquí
            String query = "let $questions := (" +
                           "  for $q in collection()/posts/row[@PostTypeId='1'] " +
                           "  order by number($q/@ViewCount) descending " +
                           "  return $q" +
                           ")" +
                           "for $q in subsequence($questions, 1, 50) " +
                           "return string-join(($q/@Title, $q/@Body), ' ')";

            String postsText = baseXConn.executeQuery(query);
            
            // Processament del text
            String[] sentences = sentenceDetector.sentDetect(postsText);

            for (String sentence : sentences) {
                String[] tokens = tokenizer.tokenize(sentence);
                Span[] spans = nameFinder.find(tokens);

                for (Span span : spans) {
                    String entity = String.join(" ", java.util.Arrays.copyOfRange(tokens, span.getStart(), span.getEnd()));
                    uniqueEntities.add(entity+", PERSON"); // Afegeix l'entitat al conjunt per evitar duplicats.
                }
            }

            // Escriptura al fitxer
            for (String entity : uniqueEntities) {
                writer.write(entity + "\n");
            }

            // Tancament de recursos
            modelInSentence.close();
            modelInToken.close();
            modelInPerson.close();
            baseXConn.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
