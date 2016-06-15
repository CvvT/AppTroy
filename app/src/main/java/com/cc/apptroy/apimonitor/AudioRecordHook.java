package com.cc.apptroy.apimonitor;

import com.cc.apptroy.hook.HookParam;
import com.cc.apptroy.util.Logger;
import com.cc.apptroy.util.RefInvoke;

import java.lang.reflect.Method;

public class AudioRecordHook extends ApiMonitorHook {

	@Override
	public void startHook() {
		// TODO Auto-generated method stub
		Method startRecordingMethod = RefInvoke.findMethodExact(
				"android.media.AudioRecord", ClassLoader.getSystemClassLoader(),
				"startRecording");
		hookhelper.hookMethod(startRecordingMethod, new AbstractBahaviorHookCallBack() {
			
			@Override
			public void descParam(HookParam param) {
				// TODO Auto-generated method stub
				Logger.log_behavior("Audio Recording ->");
			}
		});
		
	}

}
