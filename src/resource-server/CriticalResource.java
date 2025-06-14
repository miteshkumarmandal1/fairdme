import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class CriticalResource {
    private static final int PORT = 9001;
    private static final String FILE_NAME = "criticalfile.txt";
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("CriticalResource server listening on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private static void handleClient(Socket socket) {
        try (
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())
        ) {
            ResourceMessage message = (ResourceMessage) in.readObject();
            String logEntry = "[ENTER] " + message.toString();

            appendToFile(logEntry);
            System.out.println(logEntry);
            
            int randomExecutionDelay = 1000 + new java.util.Random().nextInt(5001);


            Thread.sleep(randomExecutionDelay); 
            System.out.println("Critical Resource access time taken= "+randomExecutionDelay+ " ms");

            String exitEntry = "[EXIT]  " + message.toString()+"\n";
            appendToFile(exitEntry);
            System.out.println(exitEntry);

            // Send reply to client
            out.writeObject("ACK");

        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private static void appendToFile(String content) {
        try (FileWriter fw = new FileWriter(FILE_NAME, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(content);
        } catch (IOException e) {
            System.err.println("Failed to write to file: " + e.getMessage());
        }
    }
}
