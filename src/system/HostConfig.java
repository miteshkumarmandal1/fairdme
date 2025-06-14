import java.io.*;
import java.util.*;

public class HostConfig {
    public List<String> hosts = new ArrayList<>();
    public int myId = -1;

    public HostConfig(String myIP, String hostFilePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(hostFilePath));
        String line;
        while ((line = br.readLine()) != null) {
            hosts.add(line.trim());
        }
        br.close();

        for (int i = 0; i < hosts.size(); i++) {
            if (hosts.get(i).equals(myIP)) {
                myId = i;
                break;
            }
        }

        if (myId == -1) {
            throw new RuntimeException("IP " + myIP + " not found in host.txt");
        }
    }

    public int getTotalHosts() {
        return hosts.size();
    }

    public String getHost(int index) {
        return hosts.get(index);
    }
}
