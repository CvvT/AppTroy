package com.cc.apptroy.smali;

import com.cc.apptroy.util.Utility;

import org.cc.dexlib2.dexbacked.DexBackedClassDef;
import org.cc.dexlib2.dexbacked.DexBackedDexFile;
import org.cc.dexlib2.dexbacked.DexBackedField;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;

/**
 * Created by CwT on 15/12/4.
 */
public class DexBackedDeclareField extends DexBackedField {

    Field field = null;
    public DexBackedDeclareField(@Nonnull DexBackedDexFile dexfile,
                                 @Nonnull DexBackedClassDef classDef,
                                 @Nonnull Field field){
        super(dexfile, classDef, field);
        this.field = field;
    }

    @Nonnull
    @Override
    public String getName() {
        return field.getName();
    }

    @Nonnull
    @Override
    public String getType() {
        return Utility.dottoslash(field.getType().getName());
    }
}
