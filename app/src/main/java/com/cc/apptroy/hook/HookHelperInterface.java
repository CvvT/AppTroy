package com.cc.apptroy.hook;

import java.lang.reflect.Member;

/**
 * Created by CwT on 16/5/15.
 */
public interface HookHelperInterface {
    abstract void hookMethod(Member method, MethodHookCallBack callback);
}
