package cat.iesesteveterradas;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;  

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class PR32CreateMain {
    public static void main(String[] args) {
        try {
            // Conexión a BaseX
            BaseXConnection baseXConn = new BaseXConnection("localhost", 1984, "admin", "admin");
            String query = "let $rows := for $row in doc('Posts.xml')/posts/row[@PostTypeId='1'] "
               + "order by number($row/@ViewCount) descending "
               + "return $row "
               + "let $result := <rows>{subsequence($rows, 1, 10000)}</rows>"
               + "return $result";

            String result = baseXConn.executeQuery(query);
            baseXConn.close();

            // Procesar el resultado XML
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
            org.w3c.dom.Document doc = dBuilder.parse(input);

            // Conectar a MongoDB con autenticación
            MongoClient mongoClient = MongoClients.create(
                "mongodb://root:example@localhost:27017/?authSource=admin"
            );
            
            MongoDatabase database = mongoClient.getDatabase("PR132");
            MongoCollection<Document> collection = database.getCollection("questions");

            org.w3c.dom.NodeList nList = doc.getElementsByTagName("row");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                org.w3c.dom.Node nNode = nList.item(temp);
                if (nNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    org.w3c.dom.Element eElement = (org.w3c.dom.Element) nNode;
                    Document mongoDoc = new Document();

                    // Copiar y ajustar atributos para MongoDB
                    String[] attributes = {"Id", "PostTypeId", "AcceptedAnswerId", "CreationDate",
                                           "Score", "ViewCount", "Body", "OwnerUserId",
                                           "LastActivityDate", "Title", "Tags",
                                           "AnswerCount", "CommentCount", "ContentLicense"};
                    for (String attr : attributes) {
                        String value = eElement.getAttribute(attr);
                        mongoDoc.append(attr, value);
                    }

                    // Insertar documento en MongoDB
                    System.out.println("Intentando insertar documento...");
                    collection.insertOne(mongoDoc);
                    System.out.println("Documento insertado con éxito.");

                }
            }
            System.out.println("Datos insertados en MongoDB con éxito!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
