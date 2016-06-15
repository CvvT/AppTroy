package com.cc.apptroy.request;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cc.apptroy.util.Logger;

/**
 * Created by CwT on 16/5/15.
 */
public abstract class CommandHandler {

    public static String ACTION_NAME_KEY = "action";

    //Action
    final static String ACTION_DUMP_DEXFILE = "dump";
    final static String ACTION_INIT = "init";
    final static String ACTION_SHOW = "show";
    final static String ACTION_UPDATE = "update";
    final static String ACTION_UPDATE_CLASS = "update_cls";
    final static String ACTION_UPDATE_METHOD = "update_mth";
    final static String ACTION_INVOKE = "invoke";
    final static String ACTION_DUMP_MEM = "dump_mem";
    final static String ACTION_DUMP_HEAP = "dump_heap";
    final static String ACTION_HOOK_UPDATE_METHOD = "update_hook";

    //Key
    final static String COOKIE = "cookie";
    final static String FILE_PATH = "filepath";
    final static String LUA_SCRIPT = "lua";
    final static String START_DUMP_MEM = "start";
    final static String LENGTH_DUMP_MEM = "length";
    final static String CLASS_NAME = "clsName";
    final static String METHOD_NAME = "mthName";
    final static String SIGNATURE = "signature";

    public abstract void doAction();

    public static CommandHandler parserCommand(String cmd) {
        CommandHandler handler = null;
        try {
            JSONObject jsoncmd = JSONObject.parseObject(cmd);
            String action = jsoncmd.getString(ACTION_NAME_KEY);
            Logger.log("the cmd = " + action);
            if (ACTION_DUMP_DEXFILE.equals(action)) {
                if (jsoncmd.containsKey(COOKIE)) {
                    int cookie = jsoncmd.getInteger(COOKIE);
                    handler = new BackSmaliDump(cookie);
                } else {
                    Logger.log("Please set " + COOKIE);
                }
            } else if (ACTION_INIT.equals(action)) {
                if (jsoncmd.containsKey(COOKIE)) {
                    int cookie = jsoncmd.getInteger(COOKIE);
                    handler = new BackSmaliInit(cookie);
                } else {
                    Logger.log("Please set " + COOKIE);
                }
            } else if (ACTION_SHOW.equals(action)) {
                handler = new BackSmaliShow();
            } else if (ACTION_UPDATE.equals(action)) {
                if (jsoncmd.containsKey(FILE_PATH)) {
                    String filepath = jsoncmd.getString(FILE_PATH);
                    handler = new BackSmaliUpdate(filepath);
                } else {
                    Logger.log("Please set " + FILE_PATH);
                }
            } else if (ACTION_INVOKE.equals(action)) {
                if (jsoncmd.containsKey(FILE_PATH)) {
                    String filepath = jsoncmd.getString(FILE_PATH);
                    handler = new InvokeLuaScript(filepath, InvokeLuaScript.ScriptType.FILETYPE);
                } else if (jsoncmd.containsKey(LUA_SCRIPT)) {
                    String content = jsoncmd.getString(LUA_SCRIPT);
                    handler = new InvokeLuaScript(content, InvokeLuaScript.ScriptType.TEXTTYPE);
                } else {
                    Logger.log("Please set " + FILE_PATH);
                }
            } else if (ACTION_DUMP_MEM.equals(action)) {
                if (jsoncmd.containsKey(START_DUMP_MEM)) {
                    if (jsoncmd.containsKey(LENGTH_DUMP_MEM)) {
                        int start = jsoncmd.getInteger(START_DUMP_MEM);
                        int length = jsoncmd.getInteger(LENGTH_DUMP_MEM);
                        if (jsoncmd.containsKey(FILE_PATH)) {
                            String filepath = jsoncmd.getString(FILE_PATH);
                            handler = new MemoryDump(filepath, start, length);
                        } else {
                            handler = new MemoryDump(start, length);
                        }
                    } else {
                        Logger.log("Please set " + LENGTH_DUMP_MEM);
                    }
                } else {
                    Logger.log("Please set " + START_DUMP_MEM);
                }
            } else if (ACTION_DUMP_HEAP.equals(action)) {
                handler = new HeapDumpHandler();
            } else if (ACTION_UPDATE_CLASS.equals(action)) {
                if (jsoncmd.containsKey(CLASS_NAME)) {
                    String clsName = jsoncmd.getString(CLASS_NAME);
                    handler = new BackSmaliClassUpdate(clsName);
                }
            } else if (ACTION_UPDATE_METHOD.equals(action)) {
                if (jsoncmd.containsKey(CLASS_NAME) && jsoncmd.containsKey(METHOD_NAME)
                        && jsoncmd.containsKey(SIGNATURE)) {
                    String clsName = jsoncmd.getString(CLASS_NAME);
                    String mthName = jsoncmd.getString(METHOD_NAME);
                    String signature = jsoncmd.getString(SIGNATURE);
                    handler = new BackSmaliMethodUpdate(clsName, mthName, signature);
                }
            } else if (ACTION_HOOK_UPDATE_METHOD.equals(action)) {
                if (jsoncmd.containsKey(CLASS_NAME) && jsoncmd.containsKey(METHOD_NAME)
                        && jsoncmd.containsKey(SIGNATURE)) {
                    String clsName = jsoncmd.getString(CLASS_NAME);
                    String mthName = jsoncmd.getString(METHOD_NAME);
                    String signature = jsoncmd.getString(SIGNATURE);
                    handler = new HookMethodUpdate(clsName, mthName, signature);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return handler;
    }
}