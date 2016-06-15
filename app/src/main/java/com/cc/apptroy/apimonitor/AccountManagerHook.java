package com.cc.apptroy.apimonitor;

import com.cc.apptroy.hook.HookParam;
import com.cc.apptroy.util.Logger;
import com.cc.apptroy.util.RefInvoke;

import java.lang.reflect.Method;

public class AccountManagerHook extends ApiMonitorHook {

	
	@Override
	public void startHook() {
		
		Method getAccountsMethod = RefInvoke.findMethodExact(
				"android.accounts.AccountManager", ClassLoader.getSystemClassLoader(),
				"getAccounts");
		hookhelper.hookMethod(getAccountsMethod, new AbstractBahaviorHookCallBack() {
			
			@Override
			public void descParam(HookParam param) {
				// TODO Auto-generated method stub
				Logger.log_behavior("Get Account ->");
			}
		});	
		
		Method getAccountsByTypeMethod = RefInvoke.findMethodExact(
				"android.accounts.AccountManager", ClassLoader.getSystemClassLoader(),
				"getAccountsByType",String.class);
		hookhelper.hookMethod(getAccountsByTypeMethod, new AbstractBahaviorHookCallBack() {
			
			@Override
			public void descParam(HookParam param) {
				// TODO Auto-generated method stub
				String type = (String) param.args[0];
				Logger.log_behavior("Get Account By Type ->");
				Logger.log_behavior("type :" +type);
			}
		});	
	}

}
