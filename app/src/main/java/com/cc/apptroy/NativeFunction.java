package com.cc.apptroy;

import android.util.Log;

import com.cc.apptroy.ModuleContext;
import com.cc.apptroy.smali.DexFileHeadersPointer;
import com.cc.apptroy.smali.DexFileMethod;

import org.cc.dexlib2.dexbacked.MemoryDexFileItemPointer;
import org.cc.dexlib2.dexbacked.MemoryReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Created by CwT on 15/8/4.
 */
public class NativeFunction implements MemoryReader{
    private final static String DVMNATIVE_LIB = "apptroy";
    public static NativeFunction instance;

    static {
        Log.d("cc", "start load library");
        System.loadLibrary(DVMNATIVE_LIB);
        Log.d("cc", "end load library");
    }

    public static NativeFunction getInstance(){
        if (instance == null)
            instance = new NativeFunction();
        return instance;
    }

    public native void test(ClassLoader loader);
    public native List showDexFile();
    public native String getInlineOperation();
    public native ByteBuffer dumpMemory(int start,int length);
    private native DexFileHeadersPointer getHeaderItemPtr(int cookie,int version);
    public native int getMethod(Class<?> clazz, Method method);
    public native int getConstructor(Class<?> clazz, Constructor constructor);
    public native int getclinit(Class<?> clazz);
    public native void nativeLog(String tag, String message);

    public byte[] readBytes(int start, int lenght) {
        // TODO Auto-generated method stub
        ByteBuffer data = dumpMemory(start, lenght);
        data.order(ByteOrder.LITTLE_ENDIAN);
        byte[] buffer = new byte[data.capacity()];
        data.get(buffer, 0, data.capacity());
        return buffer;
    }

    public MemoryDexFileItemPointer queryDexFileItemPointer(int cookie){
        int version = ModuleContext.getInstance().getApiLevel();
        DexFileHeadersPointer iteminfo = getHeaderItemPtr(cookie,version);
        if (iteminfo == null)
            return null;
        MemoryDexFileItemPointer pointer = new MemoryDexFileItemPointer();
        pointer.setBaseAddr(iteminfo.getBaseAddr());
        pointer.setClassCount(iteminfo.getClassCount());
        pointer.setpClassDefs(iteminfo.getpClassDefs());
        pointer.setpFieldIds(iteminfo.getpFieldIds());
        pointer.setpMethodIds(iteminfo.getpMethodIds());
        pointer.setpProtoIds(iteminfo.getpProtoIds());
        pointer.setpStringIds(iteminfo.getpStringIds());
        pointer.setpTypeIds(iteminfo.getpTypeIds());
        return pointer;

    }
}
