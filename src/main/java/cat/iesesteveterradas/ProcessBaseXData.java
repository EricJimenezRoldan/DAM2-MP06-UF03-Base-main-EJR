package cat.iesesteveterradas;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class ProcessBaseXData {
    public static void main(String[] args) {
        try {
            // Connexió a BaseX
            BaseXConnection baseXConn = new BaseXConnection("localhost", 1984, "admin", "admin");
            String query = "let $rows := for $row in doc()/posts/row[@PostTypeId='1'] "
             + "order by number($row/@ViewCount) descending "
             + "return $row "
             + "return subsequence($rows, 1, 10)";

            String result = baseXConn.executeQuery(query);
            System.out.println("XML Result: " + result);
            baseXConn.close();

            // Processar el resultat XML
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
            Document doc = dBuilder.parse(input);

            NodeList nList = doc.getElementsByTagName("row");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    // Aquí pots accedir als atributs de cada <row> com s'ha mostrat anteriorment
                    System.out.println("Id: " + eElement.getAttribute("Id"));
                    // Afegeix més atributs segons necessitis
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
