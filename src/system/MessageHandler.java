import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class MessageHandler implements Runnable {
    private int port;
    private ExecutorService executorService;

    public MessageHandler(int port, ExecutorService executorService) {
        this.port = port;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Waiting for Messages on " + port);

            while (TestConfig.isGlobal_system_active()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(new SubMessageHandler(clientSocket));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        System.out.println("Listner Thread is Stopped");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
