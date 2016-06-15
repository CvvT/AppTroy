package com.cc.apptroy.smali;

import com.google.common.collect.ImmutableList;

import org.cc.dexlib2.dexbacked.DexBackedClassDef;
import org.cc.dexlib2.dexbacked.DexBackedDexFile;
import org.cc.dexlib2.dexbacked.DexBackedMethod;
import org.cc.dexlib2.iface.MethodParameter;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created by CwT on 15/12/4.
 */
public class DexBackedClinitMethod extends DexBackedMethod {

    Class clazz;
    public DexBackedClinitMethod(@Nonnull DexBackedDexFile dexfile,
                                 @Nonnull DexBackedClassDef classDef,
                                 Class clazz,
                                 int clinit){
        super(dexfile, classDef, clinit);
        this.clazz = clazz;
    }

    @Nonnull
    @Override
    public String getName() {
        return "<clinit>";
    }

    @Nonnull
    @Override
    public String getReturnType() {
        return "V";
    }

    @Nonnull
    @Override
    public List<? extends MethodParameter> getParameters() {
        return ImmutableList.of();
    }

    @Nonnull
    @Override
    public List<String> getParameterTypes() {
        return ImmutableList.of();
    }

//    @Nullable
//    @Override
//    public DexBackedMethodImplementation getImplementation() {
//        DexFileMethod clinit = NativeFunction.getInstance().getclinit(clazz);
//        if (clinit != null) {
//            return new DexBackedMethodImplementation(dexFile, this, clinit.getCodeoffset());
//        }
//        return null;
//    }
}
