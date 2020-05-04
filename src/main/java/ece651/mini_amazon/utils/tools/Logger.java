package ece651.mini_amazon.utils.tools;

public class Logger {

    public static void logSys(String msg) {
        System.out.println("SYSTEM: " + msg);
    }

    public static void logErr(String msg) {
        System.out.println("ERROR: " + msg);
    }

    public static void logWarn(String msg) {
        System.out.println("WARNING: " + msg);
    }

    public static void logMsg(String msg) {
        System.out.println("MESSAGE: " + msg);
    }
}
