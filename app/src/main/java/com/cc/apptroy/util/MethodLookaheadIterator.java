package com.cc.apptroy.util;

import com.google.common.collect.AbstractIterator;

import org.cc.dexlib2.dexbacked.DexBackedDexFile;

import java.lang.reflect.Method;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by CwT on 15/12/3.
 */
public abstract class MethodLookaheadIterator<T> extends AbstractIterator<T> implements Iterator<T>{

    @Nonnull private final DexBackedDexFile dexfile;
    private final Method[] methods;
    private int index = 0;

    public MethodLookaheadIterator(@Nonnull DexBackedDexFile dexFile, Method[] methods){
        this.dexfile = dexFile;
        this.methods = methods;
    }

    @Nullable
    protected abstract T readNextItem(@Nonnull DexBackedDexFile dexFile, @Nonnull Method method);

    @Override
    protected T computeNext() {
        return readNextItem(this.dexfile, methods[index++]);
    }
}
