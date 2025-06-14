import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TestConfig {
    private static String SERVER_IP;

    private static boolean in_critical_section = false;
    private static boolean is_interested_in_cs = false;
    private static boolean deferred_replies = false;
    private static List<Message> deferredRepliesList;
    private static boolean[] replies;
    private static Message myReq;
    private static PriorityQueue<Message> concurrentMessageQueue;
    private static int lastRequestStaisfiedTime = 0; // initialized to zero.

    private static int id = -1;
    private static String myIp;
    private static int total_hosts = 1;
    private static HostConfig config;
    private static int total_sent_message = 0;
    private static boolean global_system_active = true;

    private static ExecutorService executor;

    // Implement sequence number arrays for mitigate first in and first out.

    public static void main(String[] args) throws Exception {

        concurrentMessageQueue = new PriorityQueue<>(Message.LamportComparator);

        System.out.println("+--------------------------------------------------+");
        System.out.println("|  A Fair Distributed Mutual Exclusion Algorithms  |");
        System.out.println("+--------------------------------------------------+");

        System.out.print("Intializing System and Registering on Server to get the other Node IP Address ");
        Scanner scanner = new Scanner(System.in);
        deferredRepliesList = new ArrayList<>();

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            myIp = localHost.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // myIp = RealIPFetcher.getRealIP();

        // Ask for server IP
        System.out.print("Enter Server IP (default 127.0.0.1): ");
        String inputIP = scanner.nextLine().trim();
        SERVER_IP = inputIP.isEmpty() ? "127.0.0.1" : inputIP;

        // Start listener thread
        new Thread(TestConfig::listenForUpdates).start();
        // Send IP to server
        sendIPToServer(myIp);
        System.out.println("Sent IP to server: " + myIp);

        try {
            Thread.sleep(5000);

            
        } catch (Exception e) {
            // TODO: handle exception
        }
       

        config = new HostConfig(myIp, "host.txt");
        id = config.myId;
        System.out.println(id);
        System.out.println("System IP Address: " + myIp);
        total_hosts = config.getTotalHosts();

        System.out.println("Total: " + total_hosts);

        replies = new boolean[config.getTotalHosts()]; // creating replies array of system size
        replies[id] = true; // because do not need to take reply from itself.

        executor = Executors.newFixedThreadPool(total_hosts + 5);
        executor.submit(new MessageHandler(Consts.messageHandlerPort, executor));

        System.out.println("Server thread started using ThreadPool for Message Handling.");

        System.out.println("Starting Node....");

        while (true) {
            System.out.print("\nEnter command: ");
            String input = scanner.nextLine().trim().toUpperCase();

            switch (input) {
                case "CS":
                    HandleCriticalSection.enterCS();
                    break;

                case "LC":
                    System.out.println("Lamport Clock Value: " + LamportClock.getTime());
                    break;

                case "TM":
                    System.out.println("Total Messages Sent: " + total_sent_message);
                    break;

                case "STOP":
                    System.out.println("Stopping system. Goodbye!");
                    break;

                case "ID":
                    System.out.println(
                            "Current Node Id: " + id + " and IP Address: " + myIp + "and Total Host: " + total_hosts);
                    break;

                case "RESET":
                    total_sent_message = 0;
                    System.out.println("Mumber of Sent Messeges set to: " + total_sent_message);
                    break;

                default:
                    System.out.println("Unknown command. Try CS, LC, TM, RESET, ID, or STOP.");
            }

            if (input.equals("STOP"))
                break;
        }

        scanner.close();
        sendRemoveRequestToServer(myIp);
        System.out.println("Sent REMOVE request to Register Server and exiting...");

        // Shutting down routines:-
        System.out.println("Shuting down Node");

        System.out.println("Total Message sent: " + total_sent_message);

        // Closing other Listner threads
        try {
            global_system_active = false;
            Socket tempSocket = new Socket(myIp, Consts.messageHandlerPort);
            ObjectOutputStream tempOut = new ObjectOutputStream(tempSocket.getOutputStream());
            tempOut.writeObject("Shutdonw");
            tempSocket.close();
        } catch (Exception e) {

        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

    }

    // getters and setters.

    public static PriorityQueue<Message> getConcurrentMessageQueue() {
        return concurrentMessageQueue;
    }

    public static void setConcurrentMessageQueue(PriorityQueue<Message> queue) {
        concurrentMessageQueue = queue;
    }

    public static boolean isIn_critical_section() {
        return in_critical_section;
    }

    public synchronized static void setIn_critical_section(boolean in_critical_section) {
        TestConfig.in_critical_section = in_critical_section;
    }

    public static boolean isIs_interested_in_cs() {
        return is_interested_in_cs;
    }

    public synchronized static void setIs_interested_in_cs(boolean is_interested_in_cs) {
        TestConfig.is_interested_in_cs = is_interested_in_cs;
    }

    public static boolean isDeferred_replies() {
        return deferred_replies;
    }

    public synchronized static void setDeferred_replies(boolean deferred_replies) {
        TestConfig.deferred_replies = deferred_replies;
    }

    public static List<Message> getDeferredRepliesList() {
        return deferredRepliesList;
    }

    public synchronized static void setDeferredRepliesList(List<Message> deferredRepliesList) {
        TestConfig.deferredRepliesList = deferredRepliesList;
    }

    public static boolean[] getReplies() {
        return replies;
    }

    public synchronized static void setReplies(boolean[] replies) {
        TestConfig.replies = replies;
    }

    public static Message getMyReq() {
        return myReq;
    }

    public synchronized static void setMyReq(Message myReq) {
        TestConfig.myReq = myReq;
    }

    public static int getId() {
        return id;
    }

    public static String getMyIp() {
        return myIp;
    }

    public static int getTotal_hosts() {
        return total_hosts;
    }

    public static HostConfig getConfig() {
        return config;
    }

    public static int getTotal_sent_message() {
        return total_sent_message;
    }

    public synchronized static int incrementTotal_sent_message() {
        total_sent_message++;
        return total_sent_message;
    }

    public static boolean isGlobal_system_active() {
        return global_system_active;
    }

    public synchronized static int getLastRequestStaisfiedTime() {
        return lastRequestStaisfiedTime;
    }

    public synchronized static void setLastRequestStaisfiedTime(int lastRequestStaisfiedTime) {
        TestConfig.lastRequestStaisfiedTime = lastRequestStaisfiedTime;
    }

    private static void sendIPToServer(String myIP) throws IOException {
        Socket socket = new Socket(SERVER_IP, Consts.SERVER_PORT);
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(myIP);
        out.close();
        socket.close();
    }

    private static void sendRemoveRequestToServer(String myIP) {
        try {
            Socket socket = new Socket(SERVER_IP, Consts.SERVER_PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject("REMOVE");
            out.writeObject(myIP);
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Failed to send REMOVE to server.");
        }
    }

    private static void listenForUpdates() {
        try (ServerSocket serverSocket = new ServerSocket(Consts.LISTEN_PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                List<String> updatedHosts = (List<String>) in.readObject();

                try (BufferedWriter writer = new BufferedWriter(new FileWriter("host.txt", false))) {
                    for (String host : updatedHosts) {
                        writer.write(host);
                        writer.newLine();
                    }
                    System.out.println("Updated host.txt with " + updatedHosts.size() + " entries.");
                }

                in.close();
                socket.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
