package com.cc.apptroy.hook;

/**
 * Created by CwT on 16/5/15.
 */
public class HookHelperFacktory {

    private static HookHelperInterface hookHelper;

    public static HookHelperInterface getHookHelper(){
        if(hookHelper == null)
            hookHelper = new XposeHookHelperImpl();
        return hookHelper;
    }
}
