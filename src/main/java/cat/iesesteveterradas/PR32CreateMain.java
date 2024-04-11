package cat.iesesteveterradas;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

public class PR32CreateMain {
    private static final Logger logger = Logger.getLogger(PR32CreateMain.class.getName());

    public static void main(String[] args) {
        try {
            FileHandler fh = new FileHandler("./data/logs/PR32CreateMain.java.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // Connexió a BaseX
            BaseXConnection baseXConn = new BaseXConnection("localhost", 1984, "admin", "admin");

            String query = "let $questions := (" +
                           "  for $q in collection()/posts/row[@PostTypeId='1'] " +
                           "  order by number($q/@ViewCount) descending " +
                           "  return $q" +
                           ")" +
                           "for $q in subsequence($questions, 1, 50) " +
                           "return string-join(($q/@Title, $q/@Body), ' ')";
            String result = baseXConn.executeQuery(query);
            baseXConn.close();

            // Parsejar el resultat de BaseX
            List<Question> questions = parseQuestionsFromBaseXResult(result);

            // Connexió a MongoDB i inserció de documents
            MongoClient mongoClient = MongoClients.create("mongodb://root:example@localhost:27017");
            MongoDatabase database = mongoClient.getDatabase("PR132");
            MongoCollection<org.bson.Document> collection = database.getCollection("questions", org.bson.Document.class);

            for (Question question : questions.subList(0, Math.min(10000, questions.size()))) {
                org.bson.Document bsonDocument = new org.bson.Document()
                    .append("Id", question.id)
                    .append("PostTypeId", question.postTypeId)
                    .append("AcceptedAnswerId", question.acceptedAnswerId)
                    .append("CreationDate", question.creationDate)
                    .append("Score", question.score)
                    .append("ViewCount", question.viewCount)
                    .append("Body", question.body)
                    .append("OwnerUserId", question.ownerUserId)
                    .append("LastActivityDate", question.lastActivityDate)
                    .append("Title", question.title)
                    .append("Tags", question.tags)
                    .append("AnswerCount", question.answerCount)
                    .append("CommentCount", question.commentCount)
                    .append("ContentLicense", question.contentLicense);
                collection.insertOne(bsonDocument);
            }

            logger.info("Inserció completada amb èxit");
        } catch (Exception e) {
            logger.severe("S'ha produït un error: " + e.toString());
        }
    }

    private static List<Question> parseQuestionsFromBaseXResult(String xmlResult) throws Exception {
        List<Question> questions = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream input = new ByteArrayInputStream(xmlResult.getBytes("UTF-8"));
        Document doc = builder.parse(input);
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("row");
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                Question question = new Question(
                        eElement.getAttribute("Id"),
                        eElement.getAttribute("PostTypeId"),
                        eElement.getAttribute("AcceptedAnswerId"),
                        eElement.getAttribute("CreationDate"),
                        eElement.getAttribute("Score"),
                        eElement.getAttribute("ViewCount"),
                        eElement.getAttribute("Body"),
                        eElement.getAttribute("OwnerUserId"),
                        eElement.getAttribute("LastActivityDate"),
                        eElement.getAttribute("Title"),
                        eElement.getAttribute("Tags"),
                        eElement.getAttribute("AnswerCount"),
                        eElement.getAttribute("CommentCount"),
                        eElement.getAttribute("ContentLicense")
                );
                questions.add(question);
            }
        }
        return questions;
    }
}
