package com.cc.apptroy.request;

import com.cc.apptroy.ModuleContext;
import com.cc.apptroy.collect.HeapDump;
import com.cc.apptroy.util.Logger;

/**
 * Created by CwT on 16/5/15.
 */
public class HeapDumpHandler extends CommandHandler {

    private static String dumpFileName;

    public HeapDumpHandler() {
        dumpFileName = android.os.Process.myPid()+".hprof";
    }

    @Override
    public void doAction() {
        String heapfilePath = ModuleContext.getInstance().getAppContext().getFilesDir()+"/"+dumpFileName;
        HeapDump.dumpHeap(heapfilePath);
        Logger.log("the heap data save to ="+ heapfilePath);
    }
}
