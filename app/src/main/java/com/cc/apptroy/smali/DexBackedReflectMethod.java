package com.cc.apptroy.smali;

import org.cc.dexlib2.dexbacked.DexBackedDexFile;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

/**
 * Created by CwT on 16/4/10.
 */
public class DexBackedReflectMethod extends DexBackedVirtualMethod {

    protected final String defineclass;

    public DexBackedReflectMethod(@Nonnull DexBackedDexFile dexfile, Class<?> clazz, Method method) {
        super(dexfile, null, clazz, method);
        defineclass = "L" + clazz.getName().replaceAll("\\.", "/") + ";";
    }

    @Nonnull @Override public String getDefiningClass() { return defineclass; }

}
