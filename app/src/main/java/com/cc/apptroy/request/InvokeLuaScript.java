package com.cc.apptroy.request;

import com.cc.apptroy.collect.LuaScriptInvoker;
import com.cc.apptroy.util.Logger;

/**
 * Created by CwT on 16/5/15.
 */
public class InvokeLuaScript extends CommandHandler {

    private String script;
    private String filePath;
    private ScriptType type;

    public enum ScriptType {
        TEXTTYPE, FILETYPE
    }

    public InvokeLuaScript(String str, ScriptType type) {
        this.type = type;
        if (type == ScriptType.TEXTTYPE)
            this.script = str;
        else if (type == ScriptType.FILETYPE)
            this.filePath = str;
    }

    @Override
    public void doAction() {
        Logger.log("The Script invoke start");
        if (this.type == ScriptType.TEXTTYPE) {
            LuaScriptInvoker.getInstance().invokeScript(script);
        } else if (this.type == ScriptType.FILETYPE) {
            LuaScriptInvoker.getInstance().invokeFileScript(filePath);
        } else {
            Logger.log("the script type is invalid");
        }
        Logger.log("The Script invoke end");
    }
}
