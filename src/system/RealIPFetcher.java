import java.net.*;
import java.util.Enumeration;

public final class RealIPFetcher {
    public static String getRealIP(){
        String add="Not Found";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                // Skip down or loopback interfaces
                if (!iface.isUp() || iface.isLoopback())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        add=addr.getHostAddress();
                        System.out.println("Real IP Address: " + add);
                        return add; // Found a valid address, exit
                    }
                }
            }

            System.out.println("No external IP address found.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    return add;
    }
}