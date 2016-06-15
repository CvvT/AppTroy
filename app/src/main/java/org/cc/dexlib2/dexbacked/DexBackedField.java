/*
 * Copyright 2012, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.cc.dexlib2.dexbacked;

import org.cc.dexlib2.base.reference.BaseFieldReference;
import org.cc.dexlib2.dexbacked.raw.FieldIdItem;
import org.cc.dexlib2.dexbacked.util.AnnotationsDirectory;
import org.cc.dexlib2.dexbacked.util.StaticInitialValueIterator;
import org.cc.dexlib2.iface.ClassDef;
import org.cc.dexlib2.iface.Field;
import org.cc.dexlib2.iface.value.EncodedValue;
import org.cc.dexlib2.immutable.value.ImmutableByteEncodedValue;
import org.cc.dexlib2.immutable.value.ImmutableCharEncodedValue;
import org.cc.dexlib2.immutable.value.ImmutableDoubleEncodedValue;
import org.cc.dexlib2.immutable.value.ImmutableFloatEncodedValue;
import org.cc.dexlib2.immutable.value.ImmutableIntEncodedValue;
import org.cc.dexlib2.immutable.value.ImmutableLongEncodedValue;
import org.cc.dexlib2.immutable.value.ImmutableShortEncodedValue;
import org.cc.dexlib2.immutable.value.ImmutableStringEncodedValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.lang.reflect.Modifier;
import java.util.Set;

public class DexBackedField extends BaseFieldReference implements Field {
    @Nonnull public final DexBackedDexFile dexFile;
    @Nonnull public final ClassDef classDef;

    public final int accessFlags;
    @Nullable public EncodedValue initialValue;
    public final int annotationSetOffset;

    public final int fieldIndex;

    private int fieldIdItemOffset;

    public DexBackedField(@Nonnull DexBackedDexFile dexfile,
                          @Nonnull DexBackedClassDef classDef,
                          @Nonnull java.lang.reflect.Field field){
        this.dexFile = dexfile;
        this.classDef = classDef;
        this.fieldIndex = 0;
        this.accessFlags = field.getModifiers();
        this.annotationSetOffset = 0;
        try {
            if (Modifier.isStatic(this.accessFlags)) {
                switch (field.getName()) {
                    case "byte":
                        this.initialValue = new ImmutableByteEncodedValue(field.getByte(null));
                        break;
                    case "short":
                        this.initialValue = new ImmutableShortEncodedValue(field.getShort(null));
                        break;
                    case "char":
                        this.initialValue = new ImmutableCharEncodedValue(field.getChar(null));
                        break;
                    case "int":
                        this.initialValue = new ImmutableIntEncodedValue(field.getInt(null));
                        break;
                    case "long":
                        this.initialValue = new ImmutableLongEncodedValue(field.getLong(null));
                        break;
                    case "float":
                        this.initialValue = new ImmutableFloatEncodedValue(field.getFloat(null));
                        break;
                    case "double":
                        this.initialValue = new ImmutableDoubleEncodedValue(field.getDouble(null));
                        break;
                    case "java.lang.String":
                        this.initialValue = new ImmutableStringEncodedValue((String)field.get(null));
                        break;
                    default:
                        this.initialValue = null;
                        break;
                }
            }else {
                this.initialValue = null;
            }
        }catch (IllegalAccessException e){
            e.printStackTrace();
            this.initialValue = null;
        }
    }

    public DexBackedField(@Nonnull DexReader reader,
                          @Nonnull DexBackedClassDef classDef,
                          int previousFieldIndex,
                          @Nonnull StaticInitialValueIterator staticInitialValueIterator,
                          @Nonnull AnnotationsDirectory.AnnotationIterator annotationIterator) {
        this.dexFile = reader.dexBuf;
        this.classDef = classDef;

        // large values may be used for the index delta, which cause the cumulative index to overflow upon
        // addition, effectively allowing out of order entries.
        int fieldIndexDiff = reader.readLargeUleb128();
        this.fieldIndex = fieldIndexDiff + previousFieldIndex;
        this.accessFlags = reader.readSmallUleb128();

        this.annotationSetOffset = annotationIterator.seekTo(fieldIndex);
        this.initialValue = staticInitialValueIterator.getNextOrNull();
    }

    public DexBackedField(@Nonnull DexReader reader,
                          @Nonnull DexBackedClassDef classDef,
                          int previousFieldIndex,
                          @Nonnull AnnotationsDirectory.AnnotationIterator annotationIterator) {
        this.dexFile = reader.dexBuf;
        this.classDef = classDef;

        // large values may be used for the index delta, which cause the cumulative index to overflow upon
        // addition, effectively allowing out of order entries.
        int fieldIndexDiff = reader.readLargeUleb128();
        this.fieldIndex = fieldIndexDiff + previousFieldIndex;
        this.accessFlags = reader.readSmallUleb128();

        this.annotationSetOffset = annotationIterator.seekTo(fieldIndex);
        this.initialValue = null;
    }

    @Nonnull
    @Override
    public String getName() {
        return dexFile.getString(dexFile.readSmallUint(getFieldIdItemOffset() + FieldIdItem.NAME_OFFSET));
    }

    @Nonnull
    @Override
    public String getType() {
        return dexFile.getType(dexFile.readUshort(getFieldIdItemOffset() + FieldIdItem.TYPE_OFFSET));
    }

    @Nonnull @Override public String getDefiningClass() { return classDef.getType(); }
    @Override public int getAccessFlags() { return accessFlags; }
    @Nullable @Override public EncodedValue getInitialValue() { return initialValue; }

    @Nonnull
    @Override
    public Set<? extends DexBackedAnnotation> getAnnotations() {
        return AnnotationsDirectory.getAnnotations(dexFile, annotationSetOffset);
    }

    /**
     * Skips the reader over the specified number of encoded_field structures
     *
     * @param reader The reader to skip
     * @param count The number of encoded_field structures to skip over
     */
    public static void skipFields(@Nonnull DexReader reader, int count) {
        for (int i=0; i<count; i++) {
            reader.skipUleb128();
            reader.skipUleb128();
        }
    }

    private int getFieldIdItemOffset() {
        if (fieldIdItemOffset == 0) {
            fieldIdItemOffset = dexFile.getFieldIdItemOffset(fieldIndex);
        }
        return fieldIdItemOffset;
    }
}
