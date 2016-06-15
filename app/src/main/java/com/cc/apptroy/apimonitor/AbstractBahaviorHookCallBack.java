package com.cc.apptroy.apimonitor;

import com.cc.apptroy.hook.HookParam;
import com.cc.apptroy.hook.MethodHookCallBack;
import com.cc.apptroy.util.Logger;

/**
 * Created by CwT on 16/5/16.
 */
public abstract class AbstractBahaviorHookCallBack extends MethodHookCallBack {

    @Override
    public void beforeHookedMethod(HookParam param) {
        // TODO Auto-generated method stub
        Logger.log_behavior("Invoke "+ param.method.getDeclaringClass().getName()+"->"+param.method.getName());
        this.descParam(param);
        //this.printStackInfo();
    }

    @Override
    public void afterHookedMethod(HookParam param) {
        // TODO Auto-generated method stub
        //Logger.log_behavior("End Invoke "+ param.method.toString());
    }

    private void printStackInfo(){
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        if(stackElements != null){
            StackTraceElement st;
            for(int i=0; i<stackElements.length; i++){
                st = stackElements[i];
                if(st.getClassName().startsWith("com.android.reverse")||st.getClassName().startsWith("de.robv.android.xposed.XposedBridge"))
                    continue;
                Logger.log_behavior(st.getClassName()+":"+st.getMethodName()+":"+st.getFileName()+":"+st.getLineNumber());
            }
        }
    }

    public abstract void descParam(HookParam param);


}

