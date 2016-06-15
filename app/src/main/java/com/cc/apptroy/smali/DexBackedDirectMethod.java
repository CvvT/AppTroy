package com.cc.apptroy.smali;

import com.cc.apptroy.util.Utility;
import com.google.common.collect.ImmutableList;

import org.cc.dexlib2.dexbacked.DexBackedClassDef;
import org.cc.dexlib2.dexbacked.DexBackedDexFile;
import org.cc.dexlib2.dexbacked.DexBackedMethod;
import org.cc.dexlib2.dexbacked.util.FixedSizeList;
import org.cc.dexlib2.dexbacked.util.ParameterIterator;
import org.cc.dexlib2.iface.MethodParameter;
import org.cc.util.AbstractForwardSequentialList;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created by CwT on 15/12/4.
 */
public class DexBackedDirectMethod extends DexBackedMethod {

    protected Constructor<?> constructor;
    protected String defineclass;

    public DexBackedDirectMethod(@Nonnull DexBackedDexFile dexfile,
                                 @Nonnull DexBackedClassDef classDef,
                                 Class<?> clazz,
                                 Constructor<?> constructor){
        super(dexfile, classDef, clazz, constructor);
        this.constructor = constructor;
    }

    @Nonnull
    @Override
    public String getName() {
        return "<init>";
    }

    @Nonnull
    @Override
    public String getReturnType() {
        return "V";
    }

    @Nonnull
    @Override
    public List<? extends MethodParameter> getParameters() {
        final List<String> parameterTypes = getParameterTypes();
        if (parameterTypes.size() > 0) {
            return new AbstractForwardSequentialList<MethodParameter>() {
                @Nonnull
                @Override
                public Iterator<MethodParameter> iterator() {
                    return new ParameterIterator(parameterTypes,
                            getParameterAnnotations(),
                            getParameterNames());
                }

                @Override
                public int size() {
                    return parameterTypes.size();
                }
            };
        }
        return ImmutableList.of();
    }

    @Nonnull
    @Override
    public List<String> getParameterTypes() {
        final Class<?> types[] = constructor.getParameterTypes();
        if (types.length > 0)
            return new FixedSizeList<String>() {
                @Nonnull
                @Override
                public String readItem(int index) {
                    return Utility.dottoslash(types[index].getName());
                }

                @Override
                public int size() {
                    return types.length;
                }
            };
        return ImmutableList.of();
    }
}
