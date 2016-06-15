package com.cc.apptroy.request;

import com.cc.apptroy.smali.MemoryBackSmali;

/**
 * Created by CwT on 16/5/16.
 */
public class BackSmaliClassUpdate extends CommandHandler {

    String clsName;

    public BackSmaliClassUpdate(String className) {
        this.clsName = className;
    }

    @Override
    public void doAction() {
        MemoryBackSmali.disassembleClass(clsName);
    }
}
