package cat.iesesteveterradas;

import org.basex.api.client.ClientSession;
import org.basex.core.cmd.Open;

public class BaseXConnection {
    private ClientSession session;

    public BaseXConnection(String host, int port, String username, String password) throws Exception {
        session = new ClientSession(host, port, username, password);
    }

    public String executeQuery(String query) throws Exception {
        // Directly executing the XQuery
        session.execute("OPEN Print");
        return session.execute("xquery " + query);
    }

    public void close() throws Exception {
        if (session != null) {
            session.close();
        }
    }
}
