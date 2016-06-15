package com.cc.apptroy.hook;

import java.lang.reflect.Member;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by CwT on 16/5/15.
 */
public class XposeHookHelperImpl implements HookHelperInterface {

    @Override
    public void hookMethod(Member method, MethodHookCallBack callback) {
        // TODO Auto-generated method stub
        XposedBridge.hookMethod(method, callback);
    }

}
