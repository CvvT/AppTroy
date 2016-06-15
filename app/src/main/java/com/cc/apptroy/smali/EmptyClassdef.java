package com.cc.apptroy.smali;

import com.google.common.collect.ImmutableList;

import org.cc.dexlib2.dexbacked.DexBackedClassDef;
import org.cc.dexlib2.dexbacked.DexBackedDexFile;
import org.cc.dexlib2.dexbacked.DexBackedField;
import org.cc.dexlib2.dexbacked.DexBackedMethod;
import org.cc.dexlib2.dexbacked.util.AnnotationsDirectory;

import javax.annotation.Nonnull;

/**
 * Created by CwT on 16/4/10.
 */
public class EmptyClassdef extends DexBackedClassDef {

    public EmptyClassdef(@Nonnull DexBackedDexFile dexFile) {
        super(dexFile);
    }

    @Nonnull
    public Iterable<? extends DexBackedField> getStaticFields(final boolean skipDuplicates) {
        return ImmutableList.of();
    }

    @Nonnull
    public Iterable<? extends DexBackedField> getInstanceFields(final boolean skipDuplicates) {
        return ImmutableList.of();
    }

    @Nonnull
    public Iterable<? extends DexBackedMethod> getDirectMethods(final boolean skipDuplicates) {
        return ImmutableList.of();
    }

    @Nonnull
    public Iterable<? extends DexBackedMethod> getVirtualMethods(final boolean skipDuplicates) {
        return ImmutableList.of();
    }

    private AnnotationsDirectory getAnnotationsDirectory() {
        return AnnotationsDirectory.EMPTY;
    }
}
