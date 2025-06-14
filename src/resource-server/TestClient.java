import java.io.*;
import java.net.*;

public class TestClient {
    public static void main(String[] args) {
        try (
            Socket socket = new Socket("localhost", 9001);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            ResourceMessage msg = new ResourceMessage("Node1", InetAddress.getLocalHost().getHostAddress(), 5);
            out.writeObject(msg);

            // Wait for reply
            String response = (String) in.readObject();
            if ("ACK".equals(response)) {
                System.out.println("Server acknowledged. Proceeding to next step...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
