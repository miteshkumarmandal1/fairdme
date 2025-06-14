import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class SubMessageHandler implements Runnable {
    private Socket clientSocket;

    SubMessageHandler(Socket clienSocket) {
        this.clientSocket = clienSocket;
    }

    @Override
    public void run() {
        System.out.println("Sub Thread Created for connection: " + clientSocket);
        try {

            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

            Object obj = ois.readObject();

            Thread.sleep(10000); // Simulating a network delay of 2 seconds, so that concurrent requests can be
                                 // made.

            if (obj instanceof Message) {
                Message msg = (Message) obj;
                System.out.println("Received: " + msg + " at time " + LamportClock.getTime());
                handleMessage(msg);

            } else {
                System.out.println("Received unknown object.");
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Sub Thread exited for connection: " + clientSocket);
    }

    public void handleMessage(Message message) {

        LamportClock.update(message.clock);

        int message_type = message.type;
        switch (message_type) {
            case Consts.REQUEST: {
                if (TestConfig.isIs_interested_in_cs()) {
                    if (TestConfig.getReplies()[message.senderId]) {
                        defer(message);
                    } else {
                        TestConfig.getReplies()[message.senderId] = true;
                        TestConfig.getConcurrentMessageQueue().add(message);
                        if (HandleCriticalSection.checkForCS())
                            HandleCriticalSection.enterCS();
                    }
                } else {
                    sendReply(message.senderId);
                }
                break;
            }
            case Consts.REPLY: {
                int sender_id = message.senderId;
                TestConfig.getReplies()[sender_id] = true;
                removeHigherPriorityMessagesFromQueue(message);
                if (HandleCriticalSection.checkForCS())
                    HandleCriticalSection.enterCS();
                break;
            }
            case Consts.FLUSH: {
                System.out.println("Message: " + message);
                TestConfig.getReplies()[message.senderId] = true;
                removeHigherPriorityMessagesFromQueue(message);
                if (HandleCriticalSection.checkForCS())
                    HandleCriticalSection.enterCS();
                break;
            }

            default:
                System.out.println("No match in Messsage handling system");
        }
    }

    public void sendReply(int target_id) {
        HandleCriticalSection.sendReply(target_id, Consts.REPLY);
    }

    public void defer(Message message) {
        TestConfig.getDeferredRepliesList().add(message);
        TestConfig.setDeferred_replies(true);
        System.out.println("Message Deffered at " + LamportClock.asString() + "Message: " + message);
    }

    public void removeHigherPriorityMessagesFromQueue(Message msg2) {

        int ct = 0;
        while (true) {
            System.out.println("In remove queue while loop");

            Message msg1 = TestConfig.getConcurrentMessageQueue().peek();
            if (msg1 == null) {
                System.out.println("Queue is empty, breaking loop.");
                break;
            }
            System.out.println(TestConfig.getConcurrentMessageQueue().toString());

            if (isMsg1HighPriority(msg1, msg2)) {
                ct++;
                TestConfig.getConcurrentMessageQueue().poll();
                System.out.println("Removed Message " + ct);
            } else {
                break;
            }
        }

        System.out.println("Message Removed from LRQ, count= " + ct + " at " + LamportClock.asString());
    }

    boolean isMsg1HighPriority(Message msg1, Message msg2) {
        if (msg1.clock < msg2.lastRequestSatisfiedTime)
            return true;

        if (msg1.clock == msg2.lastRequestSatisfiedTime) {
            if (msg1.senderId <= msg2.senderId)
                return true;
        }
        return false;

    }
}
