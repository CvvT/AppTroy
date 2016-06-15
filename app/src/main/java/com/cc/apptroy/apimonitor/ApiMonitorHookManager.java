package com.cc.apptroy.apimonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CwT on 16/5/16.
 */
public class ApiMonitorHookManager {

    static ApiMonitorHookManager instance;

    List<ApiMonitorHook> list = new ArrayList<>();

    public static ApiMonitorHookManager getInstance() {
        if (instance == null)
            instance = new ApiMonitorHookManager();
        return instance;
    }

    public static ApiMonitorHookManager Default() {
        getInstance().addMonitor(new SmsManagerHook())
                .addMonitor(new TelephonyManagerHook())
                .addMonitor(new MediaRecorderHook())
                .addMonitor(new AccountManagerHook())
                .addMonitor(new ActivityManagerHook())
                .addMonitor(new AlarmManagerHook())
                .addMonitor(new ConnectivityManagerHook())
                .addMonitor(new ContentResolverHook())
                .addMonitor(new ContextImplHook())
                .addMonitor(new PackageManagerHook())
                .addMonitor(new RuntimeHook())
                .addMonitor(new ActivityThreadHook())
                .addMonitor(new AudioRecordHook())
                .addMonitor(new CameraHook())
                .addMonitor(new NetWorkHook())
                .addMonitor(new NotificationManagerHook())
                .addMonitor(new ProcessBuilderHook());
        return instance;
    }

    public ApiMonitorHookManager addMonitor(ApiMonitorHook hook) {
        list.add(hook);
        return instance;
    }

    public void startMonitor() {
        for (ApiMonitorHook hook : list) {
            hook.startHook();
        }
    }
}
