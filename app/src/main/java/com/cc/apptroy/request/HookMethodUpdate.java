package com.cc.apptroy.request;

import com.cc.apptroy.ModuleContext;
import com.cc.apptroy.hook.HookHelperFacktory;
import com.cc.apptroy.hook.HookParam;
import com.cc.apptroy.hook.MethodHookCallBack;
import com.cc.apptroy.hook.XposeHookHelperImpl;
import com.cc.apptroy.smali.MemoryBackSmali;
import com.cc.apptroy.util.Logger;
import com.cc.apptroy.util.RefInvoke;

import java.lang.reflect.Method;

/**
 * Created by CwT on 16/5/16.
 */
public class HookMethodUpdate extends CommandHandler {

    final String clsName;
    final String mthName;
    final String signature;

    public HookMethodUpdate(String clsName, String mthName, String signature) {
        this.clsName = clsName;
        this.mthName = mthName;
        this.signature = signature;
    }

    @Override
    public void doAction() {
        Method method = RefInvoke.findMethodExact(clsName,
                ModuleContext.getInstance().getBaseClassLoader(),
                mthName, signature);
        HookHelperFacktory.getHookHelper().hookMethod(method, new MethodHookCallBack() {
            @Override
            public void beforeHookedMethod(HookParam param) {
                MemoryBackSmali.disassembleClass(clsName);
                Logger.log("Trigger and update the method");
            }

            @Override
            public void afterHookedMethod(HookParam param) {

            }
        });
    }
}
