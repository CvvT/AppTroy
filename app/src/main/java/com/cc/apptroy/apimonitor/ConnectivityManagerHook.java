package com.cc.apptroy.apimonitor;

import com.cc.apptroy.hook.HookParam;
import com.cc.apptroy.util.Logger;
import com.cc.apptroy.util.RefInvoke;

import java.lang.reflect.Method;


public class ConnectivityManagerHook extends ApiMonitorHook {

	@Override
	public void startHook() {
		
		Method setMobileDataEnabledmethod = RefInvoke.findMethodExact(
				"android.net.ConnectivityManager", ClassLoader.getSystemClassLoader(),
				"setMobileDataEnabled",boolean.class);
		hookhelper.hookMethod(setMobileDataEnabledmethod, new AbstractBahaviorHookCallBack() {
			
			@Override
			public void descParam(HookParam param) {
				boolean status = (Boolean) param.args[0];
				Logger.log("Set MobileDataEnabled = "+status);
			}
		});
		
	}

}
