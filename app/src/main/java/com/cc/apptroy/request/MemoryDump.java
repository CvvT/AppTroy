package com.cc.apptroy.request;

import com.cc.apptroy.ModuleContext;
import com.cc.apptroy.collect.MemDump;

/**
 * Created by CwT on 16/5/15.
 */
public class MemoryDump extends CommandHandler {

    private String dumpFileName;
    private int start;
    private int length;

    public MemoryDump(int start, int length) {
        this.start = start;
        this.length = length;
        this.dumpFileName = "/data/data/" + ModuleContext.getInstance().getPackageName()
                + "/" + String.valueOf(start);
    }

    public MemoryDump(String filepath, int start, int length) {
        this.start = start;
        this.length = length;
        this.dumpFileName = filepath;
    }

    @Override
    public void doAction() {
        MemDump.dumpMem(dumpFileName, start, length);
    }
}
