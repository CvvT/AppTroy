package com.cc.apptroy;

import android.util.Log;

import com.cc.apptroy.request.CommandHandler;
import com.cc.apptroy.smali.DexOrJar;
import com.cc.apptroy.smali.MemoryBackSmali;
import com.cc.apptroy.util.Utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by CwT on 16/4/4.
 */
public class CommandExecutor {

    public static final int INIT = 0;
    public static final int DUMP_DEXINFO = 1;
    public static final int DUMP_BAKSMALI = 3;
    public static final int UPDATE = 2;
    public static final int RUN_SCRIPT = 4;


    public static void init() {
        Log.d("cc", "init command executor");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    execute();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void execute() {
        // for method entry event, we don't need to do anything here
        return;
    }

    public static void run(final String command) {
        Log.d("cc", "receive command:" + command);
        CommandHandler handler = CommandHandler.parserCommand(command);
        handler.doAction();
    }

    public static boolean updateClass(String className) {
        return MemoryBackSmali.disassembleClass(Utility.getClassName(className));
    }

    public static boolean updateMethod(String className, String methodName, String descriptor) {
        return MemoryBackSmali.disassembleMethod(className, methodName, descriptor);
    }

    public static boolean staticinvoke(String clsName, String mth, String signature, Object... objects) {
        try {
            Class<?> clzz = ModuleContext.getInstance().getBaseClassLoader().loadClass(clsName);
            for (Method method : clzz.getMethods()) {

                StringBuilder sb = new StringBuilder();
                sb.append("(");
                for (Class each : method.getParameterTypes()) {
                    sb.append(Utility.dottoslash(each.getName()));
                }
                sb.append(")");
                sb.append(Utility.dottoslash(method.getReturnType().getName()));
                if (sb.toString().equals(signature)) {
                    method.invoke(null, objects);
                    Log.d("cc", "method invoke successfully");
                    break;
                }
            }
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void printDexinfo(List<DexOrJar> list) {
        for (DexOrJar dex : list) {
            Log.d("cc", "cookie:" + dex.getAddr());
            Log.d("cc", "name:" +dex.getName());
        }
    }

}
