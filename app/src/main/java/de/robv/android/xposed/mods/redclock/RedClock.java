package de.robv.android.xposed.mods.redclock;

import android.content.pm.ApplicationInfo;
import android.os.Debug;
import android.util.Log;

import com.cc.apptroy.ModuleContext;
import com.cc.apptroy.PackageMetaInfo;
import com.cc.apptroy.apimonitor.ApiMonitorHookManager;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Example module which changes the color of the clock in the statusbar to red and
 * also appends a smiley to it. It does so by hooking the updateClock method of the
 * Clock class in the systemui as soon as the com.android.systemui is being loaded.
 * <br/>
 * This demonstrates how a very central component can be modified without changing
 * the APKs (including deodexing, recompiling, signing etc).
 */
public class RedClock implements IXposedHookLoadPackage{

    private static final String DUMPAPK = "com.cc.apptroy";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam == null || (loadPackageParam.appInfo.flags &
                (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0)
            return;
        Log.d("cc", "loaded app " + loadPackageParam.packageName);
        if (loadPackageParam.isFirstApplication && !DUMPAPK.equals(loadPackageParam.packageName)){
            Log.d("cc", "the package " + loadPackageParam.packageName + " has hooked");
            Log.d("cc", "the target pid is " + android.os.Process.myPid());
            PackageMetaInfo info = PackageMetaInfo.fromXposed(loadPackageParam);
            ModuleContext.getInstance().start(info);
            ApiMonitorHookManager.Default().startMonitor();
        }
        hookDebug();
        antiDebug();
    }

    public static final int DEBUG_ENABLE_DEBUGGER = 0x1;

    public void hookDebug() {
        try {
            Method start = Process.class.getMethod("start", String.class, String.class,
                    Integer.TYPE, Integer.TYPE, int[].class, Integer.TYPE, Integer.TYPE,
                    Integer.TYPE, String.class, String[].class);

            XposedBridge.hookMethod(start, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    int id = 5;
                    int flags = (Integer) methodHookParam.args[id];
                    if ((flags & DEBUG_ENABLE_DEBUGGER) == 0) {
                        flags |= DEBUG_ENABLE_DEBUGGER;
                    }
                    methodHookParam.args[id] = flags;
                    if (BuildConfig.DEBUG) {
                        XposedBridge.log("set debuggable flags to: " + flags);
                        Log.d("cc", "set it debuggable");
                    }
                }
            });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void antiDebug() {
        try {
//            Debug.isDebuggerConnected();
            Method isDebuggerConnected = Debug.class.getMethod("isDebuggerConnected");
            XposedBridge.hookMethod(isDebuggerConnected, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
//                    Log.d("cc", "set isDebuggerConnected false");
                    param.setResult(false);
                }
            });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
