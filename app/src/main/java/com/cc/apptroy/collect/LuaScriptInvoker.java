package com.cc.apptroy.collect;

import com.cc.apptroy.ModuleContext;
import com.cc.apptroy.hook.HookHelperFacktory;
import com.cc.apptroy.hook.HookHelperInterface;
import com.cc.apptroy.hook.HookParam;
import com.cc.apptroy.hook.MethodHookCallBack;
import com.cc.apptroy.util.JsonWriter;
import com.cc.apptroy.util.Logger;
import com.cc.apptroy.util.RefInvoke;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

import java.lang.reflect.Method;

/**
 * Created by CwT on 16/5/15.
 */
public class LuaScriptInvoker {

    private static LuaScriptInvoker luaInvoker;
    private final static String LUAJAVA_LIB = "luajava";
    private HookHelperInterface hookhelper = HookHelperFacktory.getHookHelper();


    private LuaScriptInvoker(){

    }


    public static LuaScriptInvoker getInstance(){
        if(luaInvoker == null)
            luaInvoker = new LuaScriptInvoker();
        return luaInvoker;
    }

//    public void start(){
//        Method findLibraryMethod = RefInvoke.findMethodExact("dalvik.system.BaseDexClassLoader", ClassLoader.getSystemClassLoader(), "findLibrary",
//                String.class);
//        hookhelper.hookMethod(findLibraryMethod, new MethodHookCallBack() {
//
//            @Override
//            public void beforeHookedMethod(HookParam param) {
//                // TODO Auto-generated method stub
//
//            }
//
//            @Override
//            public void afterHookedMethod(HookParam param) {
//                Logger.log((String) param.args[0]);
//                if (LUAJAVA_LIB.equals(param.args[0]) && param.getResult() == null) {
//                    param.setResult("/data/data/com.cc.apptroy/lib/libluajava.so");
//                }
//            }
//        });
//    }

    private void initLuaContext(LuaState luaState){
        try {
            JavaFunction logfunction = new LogFunctionCallBack(luaState);
            logfunction.register("log");
            JavaFunction tostringfunction = new ToStringFunctionCallBack(luaState);
            tostringfunction.register("tostring");
            JavaFunction bindfunction = new BindClassCallBack(luaState);
            bindfunction.register("bind");
            JavaFunction reflecttion = new ReflectCallBack(luaState);
            reflecttion.register("getMethod");
        } catch (LuaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void invokeScript(String script){
        LuaState luaState = LuaStateFactory.newLuaState();
        luaState.openLibs();
        this.initLuaContext(luaState);
        int error = luaState.LdoString(script);
        if(error!=0){
            Logger.log("Read/Parse lua error. Exit");
            return;
        }

        luaState.close();
    }

    public void invokeFileScript(String scriptFilePath){
        LuaState luaState = LuaStateFactory.newLuaState();
        luaState.openLibs();
        this.initLuaContext(luaState);
        int error = luaState.LdoFile(scriptFilePath);
        if(error!=0){
            Logger.log("Read/Parse lua error. Exit");
            return;
        }
        luaState.close();
    }

    public static class ToStringFunctionCallBack extends JavaFunction{

        public ToStringFunctionCallBack(LuaState L) {
            super(L);
        }

        @Override
        public int execute() throws LuaException {

            int param_size = this.L.getTop();
            for(int i=2; i<=param_size; i++){
                try {
                    String objDsrc = JsonWriter.parserInstanceToJson(this.getParam(i).getObject());
                    Logger.log(objDsrc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }

    }

    public static class LogFunctionCallBack extends JavaFunction{

        public LogFunctionCallBack(LuaState L) {
            super(L);
        }

        @Override
        public int execute() throws LuaException {

            int param_size = this.L.getTop();
            if(param_size ==2 ){
                String message = this.L.getLuaObject(2).getString();
                Logger.log(message);
            }

            return 0;
        }

    }

    public static class BindClassCallBack extends JavaFunction {

        @Override
        public int execute() throws LuaException {
            int param_size = this.L.getTop();
            if (param_size == 2) {
                String clsName = this.L.getLuaObject(2).getString();
                try {
                    Class<?> clazz = ModuleContext.getInstance().getBaseClassLoader().loadClass(clsName);
                    this.L.pushJavaObject(clazz);
                    return 1;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
            return 0;
        }

        public BindClassCallBack(LuaState L) { super(L);}

    }

    public static class ReflectCallBack extends JavaFunction {

        /**
         * Constructor that receives a LuaState.
         *
         * @param L LuaState object associated with this JavaFunction object
         */
        public ReflectCallBack(LuaState L) {
            super(L);
        }

        @Override
        public int execute() throws LuaException {
            int param_size = this.L.getTop();
            if (param_size == 4) {
                String clsName = this.L.getLuaObject(2).getString();
                String mthName = this.L.getLuaObject(3).getString();
                String signature = this.L.getLuaObject(4).getString();
                ClassLoader loader = ModuleContext.getInstance().getBaseClassLoader();
                if (loader != null) {
                    Method method = RefInvoke.findMethodExact(clsName, loader,
                            mthName, signature);
                    if (method != null) {
                        this.L.pushJavaObject(method);
                        return 1;
                    } else
                        return 0;
                } else {
                    Method method = RefInvoke.findMethodExact(clsName,
                            this.getClass().getClassLoader(), mthName, signature);
                    if (method != null) {
                        this.L.pushJavaObject(method);
                        return 1;
                    } else
                        return 0;
                }
            }
            return 0;
        }
    }
}
