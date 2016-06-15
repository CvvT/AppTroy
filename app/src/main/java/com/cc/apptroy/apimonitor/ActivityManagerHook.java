package com.cc.apptroy.apimonitor;

import com.cc.apptroy.hook.HookParam;
import com.cc.apptroy.util.Logger;
import com.cc.apptroy.util.RefInvoke;

import java.lang.reflect.Method;

public class ActivityManagerHook extends ApiMonitorHook {

	@Override
	public void startHook() {

		Method killBackgroundProcessesmethod = RefInvoke.findMethodExact(
				"android.app.ActivityManager", ClassLoader.getSystemClassLoader(),
				"killBackgroundProcesses", String.class);
		hookhelper.hookMethod(killBackgroundProcessesmethod, new AbstractBahaviorHookCallBack() {
			
			@Override
			public void descParam(HookParam param) {
				// TODO Auto-generated method stub
				String packageName = (String) param.args[0];
				Logger.log_behavior("kill packagename = "+packageName);
			}
		});
		
		Method forceStopPackagemethod = RefInvoke.findMethodExact(
				"android.app.ActivityManager", ClassLoader.getSystemClassLoader(),
				"forceStopPackage", String.class);
		hookhelper.hookMethod(forceStopPackagemethod, new AbstractBahaviorHookCallBack() {
			
			@Override
			public void descParam(HookParam param) {
				// TODO Auto-generated method stub
				String packageName = (String) param.args[0];
				Logger.log_behavior("kill packagename = "+packageName);
			}
		});
	}

}
