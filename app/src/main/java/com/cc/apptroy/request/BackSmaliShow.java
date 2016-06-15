package com.cc.apptroy.request;

import com.cc.apptroy.NativeFunction;

/**
 * Created by CwT on 16/5/15.
 */
public class BackSmaliShow extends CommandHandler {

    @Override
    public void doAction() {
        NativeFunction.getInstance().showDexFile();
    }
}
