package com.cc.apptroy.smali;

import com.cc.apptroy.ModuleContext;
import com.cc.apptroy.NativeFunction;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.cc.dexlib2.dexbacked.DexBackedAnnotation;
import org.cc.dexlib2.dexbacked.DexBackedClassDef;
import org.cc.dexlib2.dexbacked.DexBackedDexFile;
import org.cc.dexlib2.dexbacked.DexBackedField;
import org.cc.dexlib2.dexbacked.DexBackedMethod;
import org.cc.dexlib2.dexbacked.util.AnnotationsDirectory;
import org.cc.dexlib2.dexbacked.util.FixedSizeSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by CwT on 16/4/10.
 */
public class DexBackedReflectClassDef extends DexBackedClassDef {

    private Class<?> clazz;
    private Method[] methods = null;
    private Constructor[] cons = null;
    private int clinit = 0;
    private Field fields[] = null;
    private String type = null;

    public DexBackedReflectClassDef(@Nonnull DexBackedDexFile dexFile, String className) {
        super(dexFile);
        type = className;
        try {
            clazz = ModuleContext.getInstance().getBaseClassLoader().loadClass(className);
            this.fields = clazz.getDeclaredFields();
            this.cons = clazz.getDeclaredConstructors();
            this.methods = clazz.getDeclaredMethods();
            this.directMethodCount = cons.length;
            this.virtualMethodCount = methods.length;
            this.instanceFieldCount = fields.length;
            this.clinit = NativeFunction.getInstance().getclinit(clazz);
            if (this.clinit != 0)
                this.directMethodCount++;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public String getType() {
        return type(type);
    }

    @Nullable
    @Override
    public String getSuperclass() {
        return type(clazz.getSuperclass().getName());
    }

    @Override
    public int getAccessFlags() {
        return clazz.getModifiers();
    }

    @Nullable
    @Override
    public String getSourceFile() {
        String[] splits = type.split("\\.");
        String last =  splits[splits.length-1];
        int end = last.indexOf('$');
        if (end > 0)
            return last.substring(0, end) + ".java";
        return last + ".java";
    }

    @Nonnull
    @Override
    public Set<String> getInterfaces() {
        final Class[] types = clazz.getInterfaces();
        if (types.length > 0) {
            return new FixedSizeSet<String>() {
                @Nonnull
                @Override
                public String readItem(int index) {
                    return type(types[index].getName());
                }

                @Override
                public int size() {
                    return types.length;
                }
            };
        }
        return ImmutableSet.of();
    }

    public String type(String name) {
        if (name.length() == 1 || name.charAt(0) == '[')
            return name;
        return "L" + name.replaceAll("\\.", "/") + ";";
    }

    @Nonnull
    @Override
    public Set<? extends DexBackedAnnotation> getAnnotations() {
        return getAnnotationsDirectory().getClassAnnotations();
    }

    @Nonnull
    public Iterable<? extends DexBackedField> getStaticFields(final boolean skipDuplicates) {
        return ImmutableList.of();
    }

    @Nonnull
    public Iterable<? extends DexBackedField> getInstanceFields(final boolean skipDuplicates) {
        if (instanceFieldCount > 0) {
            return new Iterable<DexBackedField>() {
                @Nonnull
                @Override
                public Iterator<DexBackedField> iterator() {

                    return new Iterator<DexBackedField>() {
                        private int count = 0;
                        @Override
                        public boolean hasNext() {
                            return count < instanceFieldCount;
                        }

                        @Override
                        public DexBackedField next() {
                            return new DexBackedDeclareField(dexFile, DexBackedReflectClassDef.this, fields[count++]);
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        } else {
            return ImmutableSet.of();
        }
    }

    @Nonnull
    public Iterable<? extends DexBackedMethod> getDirectMethods(final boolean skipDuplicates) {
        if (directMethodCount > 0) {
            return new Iterable<DexBackedMethod>() {
                @Nonnull
                @Override
                public Iterator<DexBackedMethod> iterator() {
                    return new Iterator<DexBackedMethod>() {
                        private int count = 0;

                        @Override
                        public boolean hasNext() {
                            return count < directMethodCount;
                        }

                        @Override
                        public DexBackedMethod next() {
                            if (count == directMethodCount - 1 && clinit != 0) { //the last one is <clinit> method
                                count++;
                                return new DexBackedClinitMethod(dexFile, DexBackedReflectClassDef.this, clazz, clinit);
                            }
                            return new DexBackedDirectMethod(dexFile, DexBackedReflectClassDef.this, clazz, cons[count++]);
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        } else {
            return ImmutableSet.of();
        }
    }

    @Nonnull
    public Iterable<? extends DexBackedMethod> getVirtualMethods(final boolean skipDuplicates) {
        if (virtualMethodCount > 0) {
            return new Iterable<DexBackedMethod>() {
                @Nonnull
                @Override
                public Iterator<DexBackedMethod> iterator() {
                    return new Iterator<DexBackedMethod>() {
                        private int count = 0;

                        @Override
                        public boolean hasNext() {
                            return count < methods.length;
                        }

                        @Override
                        public DexBackedMethod next() {
                            return new DexBackedVirtualMethod(dexFile, DexBackedReflectClassDef.this, clazz, methods[count++]);
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        } else {
            return ImmutableList.of();
        }
    }

    private AnnotationsDirectory getAnnotationsDirectory() {
        return AnnotationsDirectory.EMPTY;
    }
}
