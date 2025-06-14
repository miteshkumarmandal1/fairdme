import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.PriorityQueue;

public final class HandleCriticalSection {

    public static boolean checkForCS() {
        if (!TestConfig.isIs_interested_in_cs())
            return false;
        System.out.println("Checking for CS");
        boolean test_for_cs = true;

        for (int i = 0; i < TestConfig.getTotal_hosts(); i++) {
            if (!TestConfig.getReplies()[i])
                test_for_cs = false;
        }

        if (test_for_cs && !TestConfig.isIn_critical_section()
                && TestConfig.getMyReq() == TestConfig.getConcurrentMessageQueue().peek())
            return true;
        else
            return false;

    }

    public static void enterCS() {
        System.out.println("Trying to enter in CS");
        Message tempMessage = sendRequestForCS();
        if (tempMessage != null) {
            TestConfig.setMyReq(tempMessage);
            TestConfig.getConcurrentMessageQueue().clear(); // made null.
            TestConfig.getConcurrentMessageQueue().add(tempMessage); // add the request message.
        }

        if (!checkForCS())
            return;

        TestConfig.getConcurrentMessageQueue().poll();
        TestConfig.setIn_critical_section(true);
        TestConfig.setLastRequestStaisfiedTime(TestConfig.getMyReq().clock); // changing the value to last Request
                                                                             // Satisfied..
        System.out.println("Entered in Critical Section at clock " + LamportClock.asString());

        executeInCS(0);

    }

    public static void executeInCS(int time_in_ms) {
        try {
            Thread.sleep(time_in_ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // LamportClock.increment(); // increment the clock.

        accessCriticalResource(); // Accessing critical resource, which can be accessed asynchronously.
        exitCS();
    }

    public static boolean exitCS() {
        for (int i = 0; i < TestConfig.getTotal_hosts(); i++) {
            TestConfig.getReplies()[i] = false;
        }
        TestConfig.getReplies()[TestConfig.getId()] = true;

        TestConfig.setIs_interested_in_cs(false);

        TestConfig.setIn_critical_section(false);

        Message topMessage = TestConfig.getConcurrentMessageQueue().poll();
        if (topMessage != null) {
            sendReply(topMessage.senderId, Consts.FLUSH);
        } else {
            System.out.println("No messagess in Concurrent Queue found at " + LamportClock.asString());
        }
        System.out.println(LamportClock.asString() + " Checking and Sending deferred replies...");
        sendDeferedReply();
        System.out.println("Exiting Critical Section " + LamportClock.asString());
        return true;
    }

    public static void sendDeferedReply() {
        if (TestConfig.isDeferred_replies()) {
            int defer_count = 0;
            for (Message msg : TestConfig.getDeferredRepliesList()) {
                sendReply(msg.senderId, Consts.REPLY);
                defer_count++;
            }
            System.out.println("Total " + defer_count + " deferred replies sent");
            TestConfig.setDeferred_replies(false);
        } else
            System.out.println("No Defered Replies are there right now: " + LamportClock.asString());
    }

    public static void sendReply(int target_id, int type) {
        String target_ip_address = TestConfig.getConfig().getHost(target_id);

        try {
            Socket socket = new Socket(target_ip_address, Consts.messageHandlerPort);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            LamportClock.increment();

            Message reply = new Message(LamportClock.getTime(), TestConfig.getId(), type,
                    TestConfig.getLastRequestStaisfiedTime());
            oos.writeObject(reply);
            System.out
                    .println("REPlY/FLUSH Message sent to Node" + target_id + " :" + reply + " at "
                            + LamportClock.getTime());
            // socket.close();
        } catch (UnknownHostException e) {

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        TestConfig.incrementTotal_sent_message();
    }

    public static Message sendRequestForCS() {
        if (TestConfig.isIs_interested_in_cs())
            System.out.println("Already sent Requests and current time: " + LamportClock.asString());
        else {
            LamportClock.increment();
            TestConfig.setIs_interested_in_cs(true);

            Message reqMessage = new Message(LamportClock.getTime(), TestConfig.getId(), Consts.REQUEST,
                    TestConfig.getLastRequestStaisfiedTime());
            int target_id = -1;
            for (String host : TestConfig.getConfig().hosts) {
                target_id++;
                if (host.equals(TestConfig.getMyIp()))
                    continue;
                try {
                    Socket socket = new Socket(host, Consts.messageHandlerPort);
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                    oos.writeObject(reqMessage);
                    // socket.close();
                    System.out.println("Request Sent to Node" + target_id + " :" + reqMessage + " at time "
                            + LamportClock.getTime());
                } catch (UnknownHostException e) {

                    e.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Exception at the point of Write object");
                }
                TestConfig.incrementTotal_sent_message();
            }
            return reqMessage;
        }
        return null;
    }

    public static void accessCriticalResource() {
        try (
                Socket socket = new Socket(Consts.criticlaResourceAddress, Consts.criticlaResourcePort);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            ResourceMessage msg = new ResourceMessage("Node" + TestConfig.getId(), TestConfig.getMyIp(),
                    LamportClock.getTime());
            out.writeObject(msg);

            // Wait for reply
            String response = (String) in.readObject();
            if ("ACK".equals(response)) {
                System.out.println("Accessed Asynchronous resource at Resource server");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
