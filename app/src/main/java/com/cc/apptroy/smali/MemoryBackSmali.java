package com.cc.apptroy.smali;

import android.util.Log;

import com.cc.apptroy.ModuleContext;
import com.cc.apptroy.NativeFunction;
import com.cc.apptroy.baksmali.Adaptors.ClassDefinition;
import com.cc.apptroy.baksmali.Adaptors.MethodDefinition;
import com.cc.apptroy.baksmali.baksmaliOptions;
import com.cc.apptroy.util.Utility;
import com.google.common.collect.Ordering;

import org.cc.dexlib2.Opcodes;
import org.cc.dexlib2.analysis.ClassPath;
import org.cc.dexlib2.analysis.CustomInlineMethodResolver;
import org.cc.dexlib2.dexbacked.DexBackedClassDef;
import org.cc.dexlib2.dexbacked.DexBackedDexFile;
import org.cc.dexlib2.dexbacked.MemoryDexFileItemPointer;
import org.cc.dexlib2.dexbacked.MemoryReader;
import org.cc.dexlib2.iface.ClassDef;
import org.cc.dexlib2.iface.DexFile;
import org.cc.dexlib2.iface.MethodImplementation;
import org.cc.dexlib2.util.SyntheticAccessorResolver;
import org.cc.dexlib2.util.TypeUtils;
import org.cc.util.ClassFileNameHandler;
import org.cc.util.IndentingWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MemoryBackSmali {

    static DexBackedDexFile mmDexFile = null;
    static baksmaliOptions options = null;

    private static baksmaliOptions configOptions() {

        baksmaliOptions options = new baksmaliOptions();
        options.apiLevel = ModuleContext.getInstance().getApiLevel();
        options.outputDirectory = "/data/data/"+ModuleContext.getInstance().getPackageName()+"/files/smali";
        options.allowOdex = true;
        options.deodex = true;
        options.jobs = 3;
        options.bootClassPathDirs.add("/system/framework/");
        if (options.apiLevel >= 17) {
            options.checkPackagePrivateAccess = true;
        }
        options.registerInfo = 128;
        options.noAccessorComments = false;
        options.useLocalsDirective = true;
        options.noParameterRegisters = false;
        options.useSequentialLabels = true;
		/*
		 * we counter crash when parse debuginfo, so we have to turn it off.
		 */
        options.outputDebugInfo = false;
        options.addCodeOffsets = false;
        options.bootClassPathEntries = getDefaultBootClassPathForApi(options.apiLevel);

        return options;
    }

    public static boolean init(int cookie) {
        if (options == null)
            options = configOptions();

        Opcodes opcodes = new Opcodes(ModuleContext.getInstance().getApiLevel());

        Log.d("cc", "start init");
        if (mmDexFile == null) {
            MemoryReader reader = new NativeFunction();
            MemoryDexFileItemPointer pointer = NativeFunction.getInstance()
                    .queryDexFileItemPointer(cookie);
            if (pointer == null)
                return false;
            mmDexFile = new DexBackedDexFile(opcodes, pointer,
                    reader);

            options.classPath = ClassPath.fromClassPath(options.bootClassPathDirs,
                    options.bootClassPathEntries, mmDexFile, options.apiLevel, options.experimental);
            String inlineString = NativeFunction.getInstance().getInlineOperation();
            options.inlineResolver = new CustomInlineMethodResolver(
                    options.classPath, inlineString);
        }
        return true;
    }

    public static boolean disassembleDexFile(int cookie) {

        long startTime = System.currentTimeMillis();

        if (!init(cookie)) {
            Log.d("cc", "init dex file error");
            return false;
        } else {
            Log.d("cc", "init dex file successfully");
        }

        File outputDirectoryFile = new File(options.outputDirectory);
        if (!outputDirectoryFile.exists()) {
            if (!outputDirectoryFile.mkdirs()) {
                Log.d("cc", "Can't create the output directory "
                        + options.outputDirectory);
                return false;
            }
        }

        List<? extends ClassDef> classDefs = Ordering.natural().sortedCopy(
                mmDexFile.getClasses());

        if (!options.noAccessorComments) {
            options.syntheticAccessorResolver = new SyntheticAccessorResolver(
                    classDefs);
        }

        final ClassFileNameHandler fileNameHandler = new ClassFileNameHandler(
                outputDirectoryFile, ".smali");

        ExecutorService executor = Executors.newFixedThreadPool(options.jobs);
        List<Future<Boolean>> tasks = new ArrayList<Future<Boolean>>();

        for (final ClassDef classDef : classDefs) {
            String type = classDef.getType();
            if (type.startsWith("Landroid") ||
                    type.startsWith("Ljava") ||
                    type.startsWith("Ldalvik"))
                continue;

            tasks.add(executor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return disassembleClass(classDef, fileNameHandler, options);
                }
            }));
        }

        boolean errorOccurred = false;
        try {
            for (Future<Boolean> task : tasks) {
                while (true) {
                    try {
                        if (!task.get()) {
                            errorOccurred = true;
                        }
                    } catch (InterruptedException ex) {
                        continue;
                    } catch (ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                    break;
                }
            }
        } finally {
            executor.shutdown();
        }

        NativeFunction.getInstance().nativeLog("cc", "end disassemble the mCookie: cost time = "
                + ((System.currentTimeMillis() - startTime) / 1000) + "s");
        Log.d("cc", "end disassemble the mCookie: cost time = "
                + ((System.currentTimeMillis() - startTime) / 1000) + "s");

//		startTime = System.currentTimeMillis();
//		Log.d("cc", "start build the smali files to dex");
//		boolean result = DexFileBuilder.buildDexFile(options.outputDirectory,outDexName);
//		Log.d("cc", "end build the smali files to dex: cost time = "
//				+ ((System.currentTimeMillis() - startTime) / 1000)+"s");
//		if (result) {
//			try {
//				Runtime.getRuntime().exec("rm -rf " + options.outputDirectory);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
        return !errorOccurred;

    }

    public static boolean disassembleMethod(String className, String methodName,
                                            String descriptor) {
        if (mmDexFile == null || options == null)
            return false;

        try {
            Class clazz = ModuleContext.getInstance().getBaseClassLoader().loadClass(className);
            for (Method mth : clazz.getDeclaredMethods()) {
                if (mth.getName().equals(methodName)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("(");
                    for (Class param : mth.getParameterTypes()) {
                        sb.append(Utility.dottoslash(param.getName()));
                    }
                    sb.append(")");
                    sb.append(Utility.dottoslash(mth.getReturnType().getName()));
                    if (sb.toString().equals(descriptor)) {
                        //find it
                        Log.d("cc", "find the method");
                        ClassDefinition cd = new ClassDefinition(options, new EmptyClassdef(mmDexFile));
                        org.cc.dexlib2.iface.Method method = new DexBackedReflectMethod(mmDexFile, clazz, mth);

                        IndentingWriter writer = null;
                        File smaliFile = null;

                        try {
                            smaliFile = new File(options.outputDirectory
                                    + File.separator + methodName + ".dat");
                            if (!smaliFile.exists()) {
                                if (!smaliFile.createNewFile()) {
                                    Log.d("cc", "Unable to create file " + smaliFile.toString()
                                            + " - skipping class");
                                    return false;
                                }
                            }

                            BufferedWriter bufWriter = new BufferedWriter(  // updated method data appended in the one file
                                    new OutputStreamWriter(new FileOutputStream(smaliFile, true),
                                            "UTF8"));

                            writer = new IndentingWriter(bufWriter);
                            MethodImplementation impl = method.getImplementation();

                            if (impl == null) {
                                MethodDefinition.writeEmptyMethodTo(writer, method, options);
                            }
                            else {
                                MethodDefinition methodDefinition = new MethodDefinition(cd, method, impl);
                                methodDefinition.writeTo(writer);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            smaliFile.delete();
                        } finally {
                            if (writer != null) {
                                try {
                                    writer.close();
                                } catch (Throwable ex) {
                                    Log.d("cc", "\n\nError occurred while closing file "
                                            + smaliFile.toString());
                                    ex.printStackTrace();
                                }
                            }
                        }
                        break;
                    }
                }
            }
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean disassembleClass(String className) {

        if (options == null || mmDexFile == null || className == null) {
            return false;
        }

        File outputDirectoryFile = new File(options.outputDirectory);
        if (!outputDirectoryFile.exists()) {
            if (!outputDirectoryFile.mkdirs()) {
                Log.d("cc", "Can't create the output directory "
                        + options.outputDirectory);
                return false;
            }
        }
        final ClassFileNameHandler fileNameHandler = new ClassFileNameHandler(
                outputDirectoryFile, ".smali");

        return disassembleClass(new DexBackedReflectClassDef(mmDexFile, className),
                fileNameHandler, options);
    }

    private static boolean disassembleClass(ClassDef classDef,
                                            ClassFileNameHandler fileNameHandler, baksmaliOptions options) {

        DexBackedClassDef classdf = (DexBackedClassDef) classDef;
        if (!classdf.isValid())
            return false;
        String classDescriptor = classDef.getType();
        Log.d("cwt", "start backsmali the class = " + classDescriptor);
        Log.d("cc", "start backsmali the class = " + classDescriptor);
        // validate that the descriptor is formatted like we expect
        if (classDescriptor.charAt(0) != 'L'
                || classDescriptor.charAt(classDescriptor.length() - 1) != ';') {
            Log.d("cc", "Unrecognized class descriptor - " + classDescriptor
                    + " - skipping class");
            return false;
        }

        File smaliFile = fileNameHandler
                .getUniqueFilenameForClass(classDescriptor);

        // create and initialize the top level string template
        ClassDefinition classDefinition = new ClassDefinition(options, classDef);

        // write the disassembly
        Writer writer = null;
        try {
            File smaliParent = smaliFile.getParentFile();
            if (!smaliParent.exists()) {
                if (!smaliParent.mkdirs()) {
                    // check again, it's likely it was created in a different
                    // thread
                    if (!smaliParent.exists()) {
                        Log.d("cc", "Unable to create directory "
                                + smaliParent.toString() + " - skipping class");
                        return false;
                    }
                }
            }

            if (!smaliFile.exists()) {
                if (!smaliFile.createNewFile()) {
                    Log.d("cc", "Unable to create file " + smaliFile.toString()
                            + " - skipping class");
                    return false;
                }
            }

            BufferedWriter bufWriter = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(smaliFile),
                            "UTF8"));

            writer = new IndentingWriter(bufWriter);
            classDefinition.writeTo((IndentingWriter) writer);
        } catch (Exception ex) {
            Log.d("cc", "\n\nError occurred while disassembling class "
                    + classDescriptor.replace('/', '.') + " - skipping class");
            ex.printStackTrace();
            // noinspection ResultOfMethodCallIgnored
            smaliFile.delete();
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Throwable ex) {
                    Log.d("cc", "\n\nError occurred while closing file "
                            + smaliFile.toString());
                    ex.printStackTrace();
                }
            }
        }
        return true;
    }

    private static List<String> getDefaultBootClassPathForApi(int apiLevel) {
        List<String> list = new ArrayList<String>();
        if (apiLevel < 9) {
            list.add("/system/framework/core.jar");
            list.add("/system/framework/ext.jar");
            list.add("/system/framework/framework.jar");
            list.add("/system/framework/android.policy.jar");
            list.add("/system/framework/services.jar");
        } else if (apiLevel < 12) {

            list.add("/system/framework/core.jar");
            list.add("/system/framework/bouncycastle.jar");
            list.add("/system/framework/ext.jar");
            list.add("/system/framework/framework.jar");
            list.add("/system/framework/android.policy.jar");
            list.add("/system/framework/services.jar");
            list.add("/system/framework/core-junit.jar");
        } else if (apiLevel < 14) {

            list.add("/system/framework/core.jar");
            list.add("/system/framework/apache-xml.jar");
            list.add("/system/framework/bouncycastle.jar");
            list.add("/system/framework/ext.jar");
            list.add("/system/framework/framework.jar");
            list.add("/system/framework/android.policy.jar");
            list.add("/system/framework/services.jar");
            list.add("/system/framework/core-junit.jar");
        } else if (apiLevel < 16) {

            list.add("/system/framework/core.jar");
            list.add("/system/framework/core-junit.jar");
            list.add("/system/framework/bouncycastle.jar");
            list.add("/system/framework/ext.jar");
            list.add("/system/framework/framework.jar");
            list.add("/system/framework/android.policy.jar");
            list.add("/system/framework/services.jar");
            list.add("/system/framework/apache-xml.jar");
            list.add("/system/framework/filterfw.jar");

        } else {
            // this is correct as of api 17/4.2.2

            list.add("/system/framework/core.jar");
            list.add("/system/framework/core-junit.jar");
            list.add("/system/framework/bouncycastle.jar");
            list.add("/system/framework/ext.jar");
            list.add("/system/framework/framework.jar");
//			list.add("/system/framework/telephony-common.jar");
//			list.add("/system/framework/mms-common.jar");
            list.add("/system/framework/android.policy.jar");
            list.add("/system/framework/services.jar");
            list.add("/system/framework/apache-xml.jar");
        }
        return list;
    }

}
