import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class RegisterServer {
    private static final int PORT = 5000;
    private static final List<String> clientIPs = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {
            String clientIP = (String) in.readObject();
            if ("REMOVE".equals(clientIP)) {
                String removedIP = (String) in.readObject();
                clientIPs.remove(removedIP);
                System.out.println("Removed client: " + removedIP);
            } else {
                if (!clientIPs.contains(clientIP)) {
                    clientIPs.add(clientIP);
                    System.out.println("New client added: " + clientIP);
                }
            }

            // Send updated list to all clients
            for (String ip : new ArrayList<>(clientIPs)) {
                try (
                    Socket outSocket = new Socket(ip, 6000);
                    ObjectOutputStream out = new ObjectOutputStream(outSocket.getOutputStream());
                ) {
                    out.writeObject(new ArrayList<>(clientIPs));
                } catch (IOException e) {
                    System.out.println("Failed to send to " + ip + ", removing...");
                    clientIPs.remove(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
