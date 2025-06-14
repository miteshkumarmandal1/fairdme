import java.io.Serializable;
import java.util.Comparator;

public class Message implements Serializable {
    public int clock;
    public int senderId;
    public int type;
    public int lastRequestSatisfiedTime;

    public Message(int clock, int senderId, int type, int lastRequestSatisfiedTime) {
        this.clock = clock;
        this.senderId = senderId;
        this.type = type;
        this.lastRequestSatisfiedTime = lastRequestSatisfiedTime;
    }

    @Override
    public String toString() {
        String typeStr;
        switch (type) {
            case 1:
                typeStr = "REQUEST";
                break;

            case 2:
                typeStr = "REPLY";
                break;
            case 3:
                typeStr = "FLUSH";
                break;
            default:
                typeStr = "Not Matched: Error";
                break;
        }
        return "[Msg from P" + senderId + " | clock=" + clock + " | type=" + typeStr +" | last Satisfied= "+lastRequestSatisfiedTime+ "]";
    }

    boolean compare(Message message) {
        if (this.clock > message.clock)
            return true;
        else if (this.clock == message.clock)
            return this.senderId > message.senderId;
        else if (this.clock < message.clock)
            return false;
        else {

            System.out.println("Not Comparamble: Message Format error");
            return false;
        }
    }

    // Custom comparator logic (reversed from your compare method)
    public static Comparator<Message> LamportComparator = (m1, m2) -> {
        if (m1.clock != m2.clock) {
            return Integer.compare(m1.clock, m2.clock);
        } else {
            return Integer.compare(m1.senderId, m2.senderId);
        }
    };
}
