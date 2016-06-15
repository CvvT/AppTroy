package com.cc.apptroy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cc.apptroy.request.CommandHandler;
import com.cc.apptroy.util.Logger;


/**
 * Created by CwT on 15/7/31.
 */
public class CommandBroadcastReceiver extends BroadcastReceiver {

    public static String INTENT_ACTION = "com.cc.dumpapk";
    public static String COMMAND_NAME_KEY = "cmd";
    public static String PACKAGE_NAME = "package";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("cc", intent.getAction());
        if (INTENT_ACTION.equals(intent.getAction())){
            final String cmd = intent.getStringExtra(COMMAND_NAME_KEY);
            final String packageName = intent.getStringExtra(PACKAGE_NAME);

            if (!ModuleContext.getInstance().getPackageName().equals(packageName))
                return;

            final CommandHandler handler = CommandHandler.parserCommand(cmd);
            if (handler != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        handler.doAction();
                        Log.d("cc", "command " + cmd + " executed successfully");
                    }
                }).start();
            } else {
                Logger.log("the cmd is invalid");
            }
        }
    }

//    private void test(){
//        NativeFunction.getInstance().nativeLog("cwt", "test native log");
//    }


//    private void dump_inline(){
//        String inline = NativeFunction.getInstance().getInlineOperation();
//        Log.d("cc", inline);
//    }

//    public class CommandOption{
//        public boolean decode = false;
//        public int cookie = -1;
//        public String outputName = "out.dex";
//        public boolean all_inmemory = false;
//        public int jobs = 6;
//        public String className;
//    }
}
