package com.cc.apptroy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.cc.apptroy.util.ActivityHook;
import com.cc.apptroy.util.Oncreatehook;
import com.cc.apptroy.util.RefInvoke;
import com.cc.apptroy.util.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;

import dalvik.system.DexFile;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by CwT on 15/7/31.
 */
public class ModuleContext {
    private PackageMetaInfo metaInfo;
    private int apiLevel;
    public static boolean HAS_REGISTER_LISENER = false;
    private Application firstApplication;
    private static ModuleContext moduleContext;
    private final boolean TEST_HOOK = true;

    private static HashMap<String, DexFileInfo> dynLoadedDexInfo = new HashMap<String, DexFileInfo>();

    public static ModuleContext getInstance() {
        if (moduleContext == null)
            moduleContext = new ModuleContext();
        return moduleContext;
    }

    public void start(PackageMetaInfo info){
        this.metaInfo = info;
        this.apiLevel = Utility.getApiLevel();

        initModuleContext();
        CommandExecutor.init();
    }

    public void initModuleContext() {
        String appClassName = this.getAppInfo().className;	//Class implementing the Application object,if user didn't define it, the field is null
        Log.d("cc", "application name is : " + appClassName);
        if (appClassName == null){
            XposedHelpers.findAndHookMethod(Application.class.getName(),
                    this.getBaseClassLoader(), "onCreate", new Oncreatehook());
        } else {
            Class<?> hook_application_class = null;
            try {
                hook_application_class = this.getBaseClassLoader().loadClass(appClassName);
                if (hook_application_class != null) {
                    Method hookMethod = hook_application_class.getDeclaredMethod("attachBaseContext", Context.class);
                    if (hookMethod != null)
                        XposedHelpers.findAndHookMethod(hook_application_class, "attachBaseContext", Context.class, new Oncreatehook());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            try{
                if (hook_application_class != null){
                    Method hookOncreateMethod = hook_application_class.getDeclaredMethod("onCreate", new Class[] {});
                    if (hookOncreateMethod != null)
                        XposedHelpers.findAndHookMethod(hook_application_class, "onCreate", new Oncreatehook());
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        if (TEST_HOOK){
            //the last method we try to hook
            XposedHelpers.findAndHookMethod(Activity.class.getName(),
                    this.getBaseClassLoader(), "onCreate", Bundle.class, new ActivityHook());
        }

    }
    public String getPackageName() {
        return metaInfo.getPackageName();
    }

    public String getProcssName() {
        return metaInfo.getProcessName();
    }

    public ApplicationInfo getAppInfo() {
        return metaInfo.getAppInfo();
    }

    public Context getAppContext() {
        return this.firstApplication;
    }

    public void setFirstApplication(Application ctx){
        if (this.firstApplication == null) {
            this.firstApplication = ctx;
        }
    }

    public int getApiLevel() {
        return this.apiLevel;
    }

    public String getLibPath(){
        return this.metaInfo.getAppInfo().nativeLibraryDir;
    }

    public ClassLoader getBaseClassLoader(){
        return this.metaInfo.getClassLoader();
    }

    private void setDefineClassLoader(int mCookie, ClassLoader classLoader){
        Iterator<DexFileInfo> dexinfos = dynLoadedDexInfo.values().iterator();
        DexFileInfo info = null;
        boolean flag = false;
        while(dexinfos.hasNext()){
            info = dexinfos.next();
            if(mCookie == info.getmCookie()){
                if(info.getDefineClassLoader() == null)
                    info.setDefineClassLoader(classLoader);
                flag = true;    //find it
            }
        }
        if (!flag){
            dynLoadedDexInfo.put(String.valueOf(mCookie), new DexFileInfo(String.valueOf(mCookie),
                    mCookie, classLoader));
        }
    }

    public HashMap<String, DexFileInfo> dumpDexFileInfo() {
        HashMap<String, DexFileInfo> dexs = new HashMap<>(dynLoadedDexInfo);
        Object dexPathList = RefInvoke.getFieldOjbect("dalvik.system.BaseDexClassLoader", getBaseClassLoader(), "pathList");
        Object[] dexElements = (Object[]) RefInvoke.getFieldOjbect("dalvik.system.DexPathList", dexPathList, "dexElements");
        DexFile dexFile = null;
        for (int i = 0; i < dexElements.length; i++) {
            dexFile = (DexFile) RefInvoke.getFieldOjbect("dalvik.system.DexPathList$Element", dexElements[i], "dexFile");
            String mFileName = (String) RefInvoke.getFieldOjbect("dalvik.system.DexFile", dexFile, "mFileName");
            int mCookie = RefInvoke.getFieldInt("dalvik.system.DexFile", dexFile, "mCookie");
            DexFileInfo dexinfo = new DexFileInfo(mFileName, mCookie, getBaseClassLoader());
            dexs.put(mFileName, dexinfo);
        }
        return dexs;
    }
}
