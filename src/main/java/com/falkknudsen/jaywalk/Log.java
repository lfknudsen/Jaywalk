package com.falkknudsen.jaywalk;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Log {
    /** Print extra information to the standard output.
     Initialise it to false here so that tests don't print said extra information. */
    public static boolean VERBOSE = false;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void log(Exception e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            err("Root cause: " + cause);
            cause.printStackTrace(System.err);
        }
    }

    /** If {@link #VERBOSE}, print the input string to the system console along with the current
     timestamp. */
    public static void log(Object obj) {
        if (VERBOSE)
            System.out.println(LocalTime.now().format(dtf) + ": " + obj.toString());
    }

    public static void log(Object... objs) {
        if (VERBOSE) {
            System.out.print(LocalTime.now().format(dtf) + ":");
            for (Object o : objs) {
                System.out.print(" " + o.toString());
            }
            System.out.println();
        }
    }

    /** If {@link #VERBOSE}, print the input string to the system error console
     along with the current timestamp. */
    public static void err(Object obj) {
        if (VERBOSE)
            System.err.println(LocalTime.now().format(dtf) + ": " + obj.toString());
    }
}
