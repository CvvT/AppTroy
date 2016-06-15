package com.cc.apptroy.request;

import com.cc.apptroy.smali.MemoryBackSmali;

/**
 * Created by CwT on 16/5/16.
 */
public class BackSmaliMethodUpdate extends CommandHandler {

    final String clsName;
    final String mthName;
    final String signature;

    public BackSmaliMethodUpdate(String clsName, String mthName, String signature) {
        this.clsName = clsName;
        this.mthName = mthName;
        this.signature = signature;
    }

    @Override
    public void doAction() {
        MemoryBackSmali.disassembleMethod(clsName, mthName, signature);
    }
}
