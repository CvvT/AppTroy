package com.cc.apptroy.request;

import com.cc.apptroy.smali.MemoryBackSmali;

/**
 * Created by CwT on 16/5/15.
 */
public class BackSmaliInit extends CommandHandler {

    int cookie;

    public BackSmaliInit(int cookie) {
        this.cookie = cookie;
    }

    @Override
    public void doAction() {
        MemoryBackSmali.init(cookie);
    }
}
