import java.io.Serializable;

public class ResourceMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public String nodeId;
    public String ipAddress;
    public int lamportClock;

    public ResourceMessage(String nodeId, String ipAddress, int lamportClock) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.lamportClock = lamportClock;
    }

    @Override
    public String toString() {
        return "NodeID: " + nodeId + ", IP: " + ipAddress + ", Clock: " + lamportClock;
    }
}
