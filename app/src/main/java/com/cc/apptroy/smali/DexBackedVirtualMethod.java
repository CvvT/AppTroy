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

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created by CwT on 15/12/4.
 */
public class DexBackedVirtualMethod extends DexBackedMethod {

    protected java.lang.reflect.Method method;

    public DexBackedVirtualMethod(@Nonnull DexBackedDexFile dexfile,
                                  @Nonnull DexBackedClassDef classDef,
                                  Class<?> clazz,
                                  java.lang.reflect.Method method){
        super(dexfile, classDef, clazz, method);
        this.method = method;
    }

    @Nonnull
    @Override
    public String getName() {
        return method.getName();
    }

    @Nonnull
    @Override
    public String getReturnType() {
        return Utility.dottoslash(method.getReturnType().getName());
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
        final Class<?> types[] = method.getParameterTypes();
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
