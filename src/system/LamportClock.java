public class LamportClock {
    private static int time = 0;

    // Increment the clock for internal or send events
    public static synchronized void increment() {
        time++;
        System.out.println("incremented "+asString());
    }

    // Update the clock upon receiving a message
    public static synchronized void update(int receivedTime) {
        time = Math.max(time, receivedTime) + 1;
        System.out.println("updated "+asString());
    }

    // Get the current logical clock time
    public static synchronized int getTime() {
        return time;
    }

    // (Optional) Directly set the clock (use with caution)
    public static synchronized void setTime(int newTime) {
        time = newTime;
    }

    public static synchronized String asString() {
        return "LamportClock{" + "time=" + time + '}';
    }
}

