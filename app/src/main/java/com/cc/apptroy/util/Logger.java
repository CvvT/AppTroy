package com.cc.apptroy.util;

import android.system.Os;
import android.util.Log;

import com.cc.apptroy.ModuleContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Logger {
	
	public static String LOGTAG_COMMAN = "shell";
	public static String LOGTAG_WORKFLOW = "apimonitor";
	public static boolean DEBUG_ENABLE = true;
	public static int PID = android.os.Process.myPid();
	
	public static void log(String message){
		if(DEBUG_ENABLE)
			Log.d("cc", message);
	}
	
	public static void log_behavior(String message){
		if(DEBUG_ENABLE)
			Log.d(LOGTAG_WORKFLOW + PID, message);
	}

    public static void log_file(String message) {
        log_file(message, "logger.log");
    }

	public static void log_file(String message, String filename){
        File file = new File(ModuleContext.getInstance().getAppContext().getFilesDir().getPath()+File.separator+filename);
        if (!file.exists()){
            try {
                if (!file.createNewFile())
                    return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            out.write(message+"\n");
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
}
