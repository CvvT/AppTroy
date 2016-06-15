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

import android.util.Log;

import com.cc.apptroy.NativeFunction;
import com.cc.apptroy.smali.DexFileMethod;
import com.cc.apptroy.util.Utility;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.cc.dexlib2.base.reference.BaseMethodReference;
import org.cc.dexlib2.dexbacked.raw.MethodIdItem;
import org.cc.dexlib2.dexbacked.raw.ProtoIdItem;
import org.cc.dexlib2.dexbacked.raw.TypeListItem;
import org.cc.dexlib2.dexbacked.util.AnnotationsDirectory;
import org.cc.dexlib2.dexbacked.util.FixedSizeList;
import org.cc.dexlib2.dexbacked.util.ParameterIterator;
import org.cc.dexlib2.iface.Annotation;
import org.cc.dexlib2.iface.Method;
import org.cc.dexlib2.iface.MethodImplementation;
import org.cc.dexlib2.iface.MethodParameter;
import org.cc.dexlib2.iface.instruction.Instruction;
import org.cc.util.AbstractForwardSequentialList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DexBackedMethod extends BaseMethodReference implements Method {
    @Nonnull public final DexBackedDexFile dexFile;
    @Nonnull public final DexBackedClassDef classDef;

    public final int accessFlags;

    private final int codeOffset;
    private final int parameterAnnotationSetListOffset;
    private final int methodAnnotationSetOffset;

    public final int methodIndex;

    private int methodIdItemOffset;
    private int protoIdItemOffset;
    private int parametersOffset = -1;

    public DexBackedMethod(@Nonnull DexBackedDexFile dexfile,
                           @Nonnull DexBackedClassDef classDef,
                           int address){
        this.dexFile = dexfile;
        this.classDef = classDef;
        this.methodIndex = 0;
//        System.out.println("[-]-------------------------------------");
//        System.out.println("[-]"+classDef.getType());
        this.accessFlags = 0x10008;
        if (address == 0)
            this.codeOffset = 0;
        else
            this.codeOffset = address - dexfile.getBaseAddr();
        this.methodAnnotationSetOffset = 0;
        this.parameterAnnotationSetListOffset = 0;
    }


    public DexBackedMethod(@Nonnull DexBackedDexFile dexfile,
                           @Nonnull DexBackedClassDef classDef,
                           Class<?> clazz,
                           java.lang.reflect.Method method){
        this.dexFile = dexfile;
        this.classDef = classDef;
        this.methodIndex = 0;
        System.out.println("[+]-------------------------------------");
        this.accessFlags = method.getModifiers();
        if (Modifier.isNative(this.accessFlags) || Modifier.isAbstract(this.accessFlags)){
//            Log.d("cwt", "it's native method");
            this.codeOffset = 0;
        }else {
            int address = NativeFunction.getInstance().getMethod(clazz, method);
            if (address == 0)
                this.codeOffset = 0;
            else
                this.codeOffset = address - dexfile.getBaseAddr();
        }
        this.methodAnnotationSetOffset = 0;
        this.parameterAnnotationSetListOffset = 0;
    }

    public DexBackedMethod(@Nonnull DexBackedDexFile dexfile,
                           @Nonnull DexBackedClassDef classDef,
                           Class<?> clazz,
                           Constructor<?> constructor){
        this.dexFile = dexfile;
        this.classDef = classDef;
        System.out.println("[#]-------------------------------------");
        this.accessFlags = constructor.getModifiers();
        if (Modifier.isNative(this.accessFlags) || Modifier.isAbstract(this.accessFlags)){
            this.codeOffset = 0;
        }else {
            int address = NativeFunction.getInstance().getConstructor(clazz, constructor);
//            int address = 0;
            if (address == 0)
                this.codeOffset = 0;
            else
                this.codeOffset = address - dexfile.getBaseAddr();
        }
        this.methodAnnotationSetOffset = 0;
        this.parameterAnnotationSetListOffset = 0;
        this.methodIndex = 0;
//        System.out.println("[#]access:" + this.accessFlags + " codeoffset:"+this.codeOffset);
//        MethodImplementation impl = getImplementation();
//        if (impl != null){
//            Log.d("cc", "[#]put instructions");
//            for (Instruction instruction: impl.getInstructions()){
//                Log.d("cc", "[#]" + instruction.getOpcode().name);
//                Log.d("cc", "[#]put instructions done");
//            }
//        }
    }

    public DexBackedMethod(@Nonnull DexReader reader,
                           @Nonnull DexBackedClassDef classDef,
                           int previousMethodIndex) {
        this.dexFile = reader.dexBuf;
        this.classDef = classDef;

        // large values may be used for the index delta, which cause the cumulative index to overflow upon
        // addition, effectively allowing out of order entries.
        int methodIndexDiff = reader.readLargeUleb128();
        this.methodIndex = methodIndexDiff + previousMethodIndex;
        this.accessFlags = reader.readSmallUleb128();
        /*
         *origin:readSmallUleb128
         *we find that it may be a minus number,so we must use readLargeUleb128
         */
        this.codeOffset = reader.readLargeUleb128();

        this.methodAnnotationSetOffset = 0;
        this.parameterAnnotationSetListOffset = 0;
    }

    public DexBackedMethod(@Nonnull DexReader reader,
                           @Nonnull DexBackedClassDef classDef,
                           int previousMethodIndex,
                           @Nonnull AnnotationsDirectory.AnnotationIterator methodAnnotationIterator,
                           @Nonnull AnnotationsDirectory.AnnotationIterator paramaterAnnotationIterator) {
        this.dexFile = reader.dexBuf;
        this.classDef = classDef;

        // large values may be used for the index delta, which cause the cumulative index to overflow upon
        // addition, effectively allowing out of order entries.
        int methodIndexDiff = reader.readLargeUleb128();
        this.methodIndex = methodIndexDiff + previousMethodIndex;
        this.accessFlags = reader.readSmallUleb128();
        /*
         *origin:readSmallUleb128
         *we find that it may be a minus number,so we must use readLargeUleb128
         */
        this.codeOffset = reader.readLargeUleb128();

        this.methodAnnotationSetOffset = methodAnnotationIterator.seekTo(methodIndex);
        this.parameterAnnotationSetListOffset = paramaterAnnotationIterator.seekTo(methodIndex);
//        Log.d("cwt", "[*]access:" + this.accessFlags + " codeoffset:"+this.codeOffset);
    }

    public int getMethodIndex() { return methodIndex; }
    @Nonnull @Override public String getDefiningClass() { return classDef.getType(); }
    @Override public int getAccessFlags() { return accessFlags; }

    @Nonnull
    @Override
    public String getName() {
        return dexFile.getString(dexFile.readSmallUint(getMethodIdItemOffset() + MethodIdItem.NAME_OFFSET));
    }

    @Nonnull
    @Override
    public String getReturnType() {
        return dexFile.getType(dexFile.readSmallUint(getProtoIdItemOffset() + ProtoIdItem.RETURN_TYPE_OFFSET));
    }

    @Nonnull
    @Override
    public List<? extends MethodParameter> getParameters() {
        int parametersOffset = getParametersOffset();
//        Log.d("cwt", "parametersOffset:" + parametersOffset);
        if (parametersOffset > 0) {
            final List<String> parameterTypes = getParameterTypes();
//            Log.d("cwt", "get parametertypes:" + parameterTypes.size());
//            for (String type: parameterTypes){
//                Log.d("cwt", type);
//            }

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
    public List<? extends Set<? extends DexBackedAnnotation>> getParameterAnnotations() {
//        Log.d("cwt", "get parameter annotations");
        /*
         * leave it to future
         */
        return ImmutableList.of();
//        return AnnotationsDirectory.getParameterAnnotations(dexFile, parameterAnnotationSetListOffset);
    }

    @Nonnull
    public Iterator<String> getParameterNames() {
//        Log.d("cwt", "get parameter names");
        /*
         * Somehow, there is a bug here for some samples. I have to make it right
         * by return null pointer here and just leave it to future.
         */
//        DexBackedMethodImplementation methodImpl = getImplementation();
//        if (methodImpl != null) {
//            return methodImpl.getParameterNames(null);
//        }
//        return ImmutableSet.<String>of().iterator();
        return ImmutableSet.<String>of().iterator();
    }

    @Nonnull
    @Override
    public List<String> getParameterTypes() {
        final int parametersOffset = getParametersOffset();
        if (parametersOffset > 0) {
            final int parameterCount = dexFile.readSmallUint(parametersOffset + TypeListItem.SIZE_OFFSET);
            final int paramListStart = parametersOffset + TypeListItem.LIST_OFFSET;
            return new FixedSizeList<String>() {
                @Nonnull
                @Override
                public String readItem(final int index) {
                    return dexFile.getType(dexFile.readUshort(paramListStart + 2 * index));
                }

                @Override
                public int size() {
                    return parameterCount;
                }
            };
        }
        return ImmutableList.of();
    }

    @Nonnull
    @Override
    public Set<? extends Annotation> getAnnotations() {
        return AnnotationsDirectory.getAnnotations(dexFile, methodAnnotationSetOffset);
    }

    @Nullable
    @Override
    public DexBackedMethodImplementation getImplementation() {
        /*
         * Modified(@CwT)
         */
        if (codeOffset != 0) {
            return new DexBackedMethodImplementation(dexFile, this, codeOffset);
        }
        return null;
    }

    private int getMethodIdItemOffset() {
        if (methodIdItemOffset == 0) {
            methodIdItemOffset = dexFile.getMethodIdItemOffset(methodIndex);
        }
        return methodIdItemOffset;
    }

    private int getProtoIdItemOffset() {
        if (protoIdItemOffset == 0) {
            int protoIndex = dexFile.readUshort(getMethodIdItemOffset() + MethodIdItem.PROTO_OFFSET);
            protoIdItemOffset = dexFile.getProtoIdItemOffset(protoIndex);
        }
        return protoIdItemOffset;
    }

    private int getParametersOffset() {
        if (parametersOffset == -1) {
            parametersOffset = dexFile.readSmallUint(getProtoIdItemOffset() + ProtoIdItem.PARAMETERS_OFFSET);
        }
        return parametersOffset;
    }

    /**
     * Skips the reader over the specified number of encoded_method structures
     *
     * @param reader The reader to skip
     * @param count The number of encoded_method structures to skip over
     */
    public static void skipMethods(@Nonnull DexReader reader, int count) {
        for (int i=0; i<count; i++) {
            reader.skipUleb128();
            reader.skipUleb128();
            reader.skipUleb128();
        }
    }
}
