package com.cc.apptroy.util;

import android.app.Activity;
import android.app.Application;
import android.content.IntentFilter;
import android.util.Log;

import com.cc.apptroy.CommandBroadcastReceiver;
import com.cc.apptroy.ModuleContext;

import de.robv.android.xposed.XC_MethodHook;

/**
 * Created by CwT on 15/12/6.
 */
public class ActivityHook extends XC_MethodHook {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        Log.d("cc", "before, register receiver");
        if (!ModuleContext.HAS_REGISTER_LISENER) {
            Activity app = (Activity) param.thisObject;
            IntentFilter filter = new IntentFilter(CommandBroadcastReceiver.INTENT_ACTION);
            app.registerReceiver(new CommandBroadcastReceiver(), filter);
            ModuleContext.HAS_REGISTER_LISENER = true;
            ModuleContext.getInstance().setFirstApplication(app.getApplication());
            Log.d("cc", "register over");
        }
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        Log.d("cc", "after, register receiver");
        if (!ModuleContext.HAS_REGISTER_LISENER) {
            Activity app = (Activity) param.thisObject;
            IntentFilter filter = new IntentFilter(CommandBroadcastReceiver.INTENT_ACTION);
            app.registerReceiver(new CommandBroadcastReceiver(), filter);
            ModuleContext.HAS_REGISTER_LISENER = true;
            ModuleContext.getInstance().setFirstApplication(app.getApplication());
            Log.d("cc", "register over");
        }
    }
}
