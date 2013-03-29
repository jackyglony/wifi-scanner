package com.shixunaoyou.wifiscanner.util;

public class Logger {

    private static String TAG = "WifiScanner";
    private static interface Printer {
        public void i(String tag, String msg);
        public void d(String tag, String msg);
        public void w(String tag, String msg);
        public void e(String tag, String msg);
    }

    private static Printer sPrinter;

    static {
        sPrinter = new Outputter();
    }

    public static void warning(String tag, String msg) {
        sPrinter.w(TAG,  tag + ": " + msg);
    }

    public static void debug(String tag, String msg) {
        sPrinter.d(TAG,  tag + ": " + msg);
    }

    public static void error(String tag, String msg) {
        sPrinter.e(TAG,  tag + ": " + msg);
    }

    private static class Outputter implements Printer {

        @Override
        public void i(String tag, String msg) {
            System.out.println("[" + tag + "]: " + msg);
        }

        @Override
        public void d(String tag, String msg) {
            System.out.println("[" + tag + "]: " + msg);
        }

        @Override
        public void w(String tag, String msg) {
            System.out.println("[" + tag + "]: " + msg);
        }

        @Override
        public void e(String tag, String msg) {
            System.out.println("[" + tag + "]: " + msg);
        }
    }

}
