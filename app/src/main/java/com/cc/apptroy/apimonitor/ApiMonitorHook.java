package com.cc.apptroy.apimonitor;

import com.cc.apptroy.hook.HookHelperFacktory;
import com.cc.apptroy.hook.HookHelperInterface;

/**
 * Created by CwT on 16/5/16.
 */
public abstract class ApiMonitorHook {

    protected HookHelperInterface hookhelper = HookHelperFacktory.getHookHelper();
    public static class InvokeInfo{
        private long invokeAtTime;
        private String className;
        private String methodName;
        private Object[] argv;
        private Object result;
        private Object invokeState;
    }
    public abstract void startHook();
}
