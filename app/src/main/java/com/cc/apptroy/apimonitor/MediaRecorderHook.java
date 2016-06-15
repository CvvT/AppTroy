package com.cc.apptroy.apimonitor;

import com.cc.apptroy.hook.HookParam;
import com.cc.apptroy.util.Logger;
import com.cc.apptroy.util.RefInvoke;

import java.io.FileDescriptor;
import java.lang.reflect.Method;

public class MediaRecorderHook extends ApiMonitorHook {

	@Override
	public void startHook() {

		Method startmethod = RefInvoke.findMethodExact(
				"android.media.MediaRecorder", ClassLoader.getSystemClassLoader(),
				"start");
		hookhelper.hookMethod(startmethod, new AbstractBahaviorHookCallBack() {
			
			@Override
			public void descParam(HookParam param) {
				// TODO Auto-generated method stub
				Logger.log_behavior("Media Record: Start ->");
				String mPath = (String)RefInvoke.getFieldOjbect("android.media.MediaRecorder", param.thisObject, "mPath");
				if(mPath != null)
				   Logger.log_behavior("Save Path: ->" +mPath);
				else{
					FileDescriptor mFd = (FileDescriptor) RefInvoke.getFieldOjbect("android.media.MediaRecorder", param.thisObject, "mFd");
					Logger.log_behavior("Save Path: ->" +mFd.toString());
				}
			}
		});
		
		Method stopmethod = RefInvoke.findMethodExact(
				"android.media.MediaRecorder", ClassLoader.getSystemClassLoader(),
				"stop");
		hookhelper.hookMethod(stopmethod, new AbstractBahaviorHookCallBack() {
			
			@Override
			public void descParam(HookParam param) {
				// TODO Auto-generated method stub
				Logger.log_behavior("Media Record: Stop ->");			
			}
		});
		
	}

}
