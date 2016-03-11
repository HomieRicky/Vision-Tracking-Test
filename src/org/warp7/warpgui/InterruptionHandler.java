package org.warp7.warpgui;

import java.util.ArrayList;

/**
 * Simple static class for taking the objects of interrupted threads and passing them on to the main class to be restarted
 */

public class InterruptionHandler {

    private static ArrayList<Object> exceptions = new ArrayList<>();

    public static void add(Object o) { exceptions.add(o); }

    public static boolean check() {
        if(!exceptions.isEmpty()) return true;
        return false;
    }

    public static Object getObject() {
        Object retr = exceptions.get(0);
        exceptions.remove(0);
        return retr;
    }
}
