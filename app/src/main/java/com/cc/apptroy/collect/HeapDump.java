package com.cc.apptroy.collect;

import android.os.Debug;

import java.io.IOException;

/**
 * Created by CwT on 16/5/15.
 */
public class HeapDump {

    public static void dumpHeap(String filename) {
        try {
            Debug.dumpHprofData(filename);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
