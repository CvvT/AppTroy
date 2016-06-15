//
// Created by 陈伟腾 on 15/8/5.
//

#include <jni.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <dlfcn.h>
#include "dump.h"
#include "com_cc_apptroy_NativeFunction.h"

#define OFFSET 796
//OFFSET should be:
// 816 in 4.4.2
// 796 in 4.1.2

typedef struct InlineOperation {
    void *          func;
    const char*     classDescriptor;
    const char*     methodName;
    const char*     methodSignature;
} InlineOperation;

typedef const InlineOperation* (*dvmGetInlineOpsTablePtr)();
typedef int (*dvmGetInlineOpsTableLengthPtr)();

struct CookieMap *allCookie = NULL;
static const char *dumppath = "/data/local/tmp/";
static int userDexFiles = 0;

int main(void)  __attribute__ ((__constructor__));
void saveCookie(jint cookie);

int main(void) {
    //
}

unsigned int readunsignedleb(const u1 **data) {
    unsigned int result = 0;
    unsigned int shift = 0;
    while (true) {
        u1 byte = **data;
        (*data)++;
        result |= ((byte & 0x7f) << shift);
        if ((byte & 0x80) == 0)
            break;
        shift += 7;
    }
    return result;
}

int readSignedLeb128(const u1** data){
    int result = 0;
    int shift = 0;
    int size = 32;
    while(true) {
        u1 byte = *(*data)++;
        result |= ((byte & 0x7f) << shift);
        shift += 7;
        if ((byte & 0x80) == 0)
            break;
    }
/* sign bit of byte is second high order bit (0x40) */
    if ((shift <size) && (result & (1 << shift)))
    /* sign extend */
    result |= - (1 << shift);
    return result;
}

void writeLeb128(u1** ptr, u4 data)
{
    while (true) {
        u1 out = (u1)(data & 0x7f);
        data >>= 7;
        if (data) {
            *(*ptr)++ = (u1)(out | 0x80);
        } else {
            *(*ptr)++ = out;
            break;
        }
    }
}

u4 unsignedLeb128Size(u4 data){
    u4 ret = 0;
    while(true){
        u4 out = data & 0x7f;
        ret++;
        if (out != data){
            data >>= 7;
        }else {
            break;
        }
    }
    return ret;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    jint result = -1;

    LOGE("in jni onload");
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }

    result = JNI_VERSION_1_4;
    LOGE("register success");
    return result;
}

void printNear(unsigned int *addr, int len) {
    __android_log_print(4, "cwt", "start print addr");
    for (int i = 0; i < len / 4;) {
        __android_log_print(4, "cwt", "0x%x 0x%x 0x%x 0x%x", *(addr + i), *(addr + 1 + i), *(addr + 2 + i), *(addr + 3 + i));
        i += 4;
    }
}

void saveCookie(jint cookie) {
    if (cookie == 0)
        return;
//    LOGE("cookie: %d", cookie);
    DexorJar *pDexorJar = (DexorJar *) cookie;
//    LOGE("filename: %s", pDexorJar->fileName);
    const char *skip = "/data/data/de.robv.android.xposed.installer";
    if (!strncmp(skip, pDexorJar->fileName, strlen(skip)))
        return;
    DexFile *dexfile = NULL;
    if (pDexorJar->isDex) {
        dexfile = pDexorJar->pRawDexFile->pDvmDex->pDexFile;
    } else {
        dexfile = pDexorJar->pJarFile->pDvmDex->pDexFile;
    }

    if (/*memcmp(dexfile->pOptHeader->magic, DEX_OPT_MAGIC, 4)*/0 == 0) {
        if (/*memcmp(dexfile->pOptHeader->magic + 4, DEX_OPT_MAGIC_VERS, 4)*/0 != 0) {
            LOGE("bad opt version");
            goto bad;
        }
        if (allCookie == NULL) {
            allCookie = (struct CookieMap *) malloc(sizeof(CookieMap));
            if (allCookie == 0)
                goto bad;
            allCookie->cookie = (int) cookie;
            allCookie->dexFile = dexfile;
            allCookie->next = NULL;
        } else {
            struct CookieMap *next = allCookie;
            if (next->cookie == cookie)
                goto bad;
            while (next->next != NULL) {
                if (next->cookie == cookie)
                    goto bad;
                next = next->next;
            }
            next->next = new CookieMap();
            next = next->next;
            next->cookie = (int) cookie;
            next->dexFile = dexfile;
            next->next = NULL;
        }
    }

    bad:
    return;
}

struct CookieMap *findcookie(int cookie) {
    struct CookieMap *next = allCookie;
    while (next != NULL) {
        if (next->cookie == cookie)
            break;
        LOGE("next cookie=%d", next->cookie);
        next = next->next;
    }
    if (next == NULL) {
        LOGE("unsaved cookie");
        goto bad;
    } else {
        LOGE("find cookie!");
    }
    return next;
    bad:
    return NULL;
}

JNIEXPORT jobject JNICALL Java_com_cc_apptroy_NativeFunction_showDexFile
        (JNIEnv *env, jobject obj){
    int status;
    jobject ret = NULL;
    LOGE("Begin to dump!!!");
    void *handle = dlopen("libdvm.so", RTLD_LAZY);
    if (handle == NULL){
        LOGE("can not open libdvm.so");
        return NULL;
    }

    int gdvm = (int)dlsym(handle, "gDvm");
    if (gdvm != 0){
        LOGE("get gDvm address=0x%x\n", gdvm);    //816 in 4.4.2
       userDexFiles = gdvm + OFFSET;  //the offset is found in android-4.1.2, maybe different in other version
        // userDexFiles = gdvm + 816;
    }

    dlclose(handle);

    if (gdvm == 0){
        LOGE("error in showDexFile");
        return NULL;
    }

    int userDex = *(int *)userDexFiles;
    int size = *(int *)userDex;
    int entry = *(int *)(userDex + 12);

    for (int i = 0; i < size; i++){
        int hash = *(int *)(entry + 8 * i);
        int item = *(int *)(entry + 8 * i + 4);
        if (hash == item && hash != 0){
            DexorJar *dexorjar = (DexorJar *)hash;
            LOGE("[-]cookie is = %d", hash);
            if (dexorjar->isDex == 0){
                LOGE("[*]cache file name is: %s\n", dexorjar->pJarFile->cacheFileName);
                LOGE("[*]dexfile address: %p\n", dexorjar->pJarFile->pDvmDex->pDexFile);
            }else{
                LOGE("[+]cache file name is: %s\n", dexorjar->pRawDexFile->cacheFileName);
                LOGE("[+]dexfile address: %p\n", dexorjar->pRawDexFile->pDvmDex->pDexFile);
            }
            saveCookie(hash);
        }
    }
    return NULL;
}

JNIEXPORT void JNICALL copyinfo(JNIEnv *env, jobject, jint cookie) {
    struct CookieMap *dexfile = findcookie(cookie);
    if (dexfile == NULL)
        goto bad;

    bad:;
}

void slashtodot(char *dest, const char *source) {
    strcpy(dest, source + 1);
    dest[strlen(dest) - 1] = '\0';
    for (u1 j = 0; j < strlen(dest); j++)
        if (dest[j] == '/')
            dest[j] = '.';
}

void getProtoString(char* proto_str, DexFile* dex, const DexMethodId* methodid){
    const char *tmp_str;
    char count = 0;
    const DexProtoId *protoid = dexGetProtoId(dex, methodid->protoIdx);
    const DexTypeList *params = dexGetProtoParameters(dex, protoid);
    strncpy(proto_str, "(", 1);
    count++;
    if (params != NULL) {
        for (u4 l = 0; l < params->size; l++) {
            tmp_str = dexStringByTypeIdx(dex, params->list[l].typeIdx);
            strcpy(proto_str + count, tmp_str);
            count += strlen(tmp_str);
        }
    }
    strncpy(proto_str + count, ")", 1);
    count++;
    tmp_str = dexStringByTypeIdx(dex, protoid->returnTypeIdx);
    strcpy(proto_str + count, tmp_str);
    count += strlen(tmp_str);
    proto_str[count] = '\0';
}

JNIEXPORT void JNICALL Java_com_cc_apptroy_NativeFunction_dumpDexFileByCookie
        (JNIEnv *env, jobject obj, jint cookie, jobject loader) {
    return;    
}

JNIEXPORT void JNICALL Java_com_cc_apptroy_NativeFunction_test
        (JNIEnv *env, jobject obj, jobject loader){
    return;
}

JNIEXPORT jstring JNICALL Java_com_cc_apptroy_NativeFunction_getInlineOperation
        (JNIEnv *env, jobject obj){
    int i;
    void *libdvm = dlopen("libdvm.so", RTLD_LAZY);
    if (libdvm == NULL) {
        printf("Failed to load libdvm: %s\n", dlerror());
        return NULL;
    }
    dvmGetInlineOpsTablePtr dvmGetInlineOpsTable = (const InlineOperation *(*)())dlsym(libdvm, "dvmGetInlineOpsTable");
    if (dvmGetInlineOpsTable == NULL) {
        dlerror();
        dvmGetInlineOpsTable = (const InlineOperation *(*)())dlsym(libdvm, "_Z20dvmGetInlineOpsTablev");
    }
    if (dvmGetInlineOpsTable == NULL) {
        printf("Failed to load dvmGetInlineOpsTable: %s\n", dlerror());
        dlclose(libdvm);
        return NULL;
    }
    dvmGetInlineOpsTableLengthPtr dvmGetInlineOpsTableLength = (int (*)())dlsym(libdvm, "dvmGetInlineOpsTableLength");
    if (dvmGetInlineOpsTableLength == NULL) {
        dlerror();
        dvmGetInlineOpsTableLength = (int (*)())dlsym(libdvm, "_Z26dvmGetInlineOpsTableLengthv");
    }
    if (dvmGetInlineOpsTableLength == NULL) {
        printf("Failed to load dvmGetInlineOpsTableLength: %s\n", dlerror());
        dlclose(libdvm);
        return NULL;
    }
    const InlineOperation *inlineTable = dvmGetInlineOpsTable();
    int length = dvmGetInlineOpsTableLength();

    LOGE("Get inlineTable Method");
    jclass stringbuilder = (*env).FindClass("java/lang/StringBuilder");
    jmethodID init = (*env).GetMethodID(stringbuilder, "<init>", "()V");
    jobject instance = (*env).NewObject(stringbuilder, init);
    jmethodID append = (*env).GetMethodID(stringbuilder, "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
    jmethodID tostring = (*env).GetMethodID(stringbuilder, "toString", "()Ljava/lang/String;");
    char s[256];
    for (i=0; i < length; i++) {
        InlineOperation *item = (InlineOperation *)&inlineTable[i];
        sprintf(s, "%s->%s%s\n", item->classDescriptor, item->methodName, item->methodSignature);
        (*env).CallObjectMethod(instance, append, (*env).NewStringUTF(s));
    }
    dlclose(libdvm);
    return (jstring)(*env).CallObjectMethod(instance, tostring);
}

JNIEXPORT jobject JNICALL Java_com_cc_apptroy_NativeFunction_getHeaderItemPtr
        (JNIEnv *env, jobject obj, jint cookie, jint version){
    LOGE("start to get headerItem pointer");
    struct CookieMap *dexfile = findcookie(cookie);
    DexFile *dex = NULL;
    if (dexfile == NULL)
        return NULL;
    dex = dexfile->dexFile;
    jclass Header = env->FindClass("com/cc/apptroy/smali/DexFileHeadersPointer");
    if (Header == NULL){
        env->ExceptionClear();
        LOGE("Get DexFileHeadersPointer error");
        return NULL;
    }
    jobject ret = env->AllocObject(Header);
    jfieldID stringIds = env->GetFieldID(Header, "pStringIds", "I");
    env->SetIntField(ret, stringIds, (int)dex->pStringIds);
    jfieldID typeIds = env->GetFieldID(Header, "pTypeIds", "I");
    env->SetIntField(ret, typeIds, (int)dex->pTypeIds);
    jfieldID fieldIds = env->GetFieldID(Header, "pFieldIds", "I");
    env->SetIntField(ret, fieldIds, (int)dex->pFieldIds);
    jfieldID methodIds = env->GetFieldID(Header, "pMethodIds", "I");
    env->SetIntField(ret, methodIds, (int)dex->pMethodIds);
    jfieldID protoIds = env->GetFieldID(Header, "pProtoIds", "I");
    env->SetIntField(ret, protoIds, (int)dex->pProtoIds);
    jfieldID classDef = env->GetFieldID(Header, "pClassDefs", "I");
    env->SetIntField(ret, classDef, (int)dex->pClassDefs);
    jfieldID classCount = env->GetFieldID(Header, "classCount", "I");
    env->SetIntField(ret, classCount, (int)dex->pHeader->classDefsSize);
    jfieldID base = env->GetFieldID(Header, "baseAddr", "I");
    env->SetIntField(ret, base, (int)dex->baseAddr);
    return ret;
}

JNIEXPORT jobject JNICALL Java_com_cc_apptroy_NativeFunction_dumpMemory
        (JNIEnv *env, jobject obj, jint offset, jint length){
    jobject ret = env->NewDirectByteBuffer((void *)offset, length);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_cc_apptroy_NativeFunction_getMethod
        (JNIEnv *env, jobject obj, jclass clazz, jobject element){
    jmethodID getMethodName, getSig;
    jclass methodClass;
    jint ret = 0;

    methodClass = env->FindClass("java/lang/reflect/Method");

    if (methodClass == NULL) {
        LOGE("error: didn't find class");
        return 0;
    } else
        LOGE("get Class");

    getMethodName = env->GetMethodID(methodClass, "getName", "()Ljava/lang/String;");
    getSig = env->GetMethodID(methodClass, "getSignature", "()Ljava/lang/String;");

    if (getMethodName == NULL || getSig == NULL){
        LOGE("error: not get methodID");
        return 0;
    } else{
        LOGE("get methodID");
    }

    jstring str = (jstring)env->CallObjectMethod(element, getMethodName);
    const char* name = env->GetStringUTFChars(str, 0);
    LOGE("Name: %s", name);
    jstring sigstr = (jstring)env->CallObjectMethod(element, getSig);
    const char* sig = env->GetStringUTFChars(sigstr, 0);
    char* sigdup = strdup(sig);
    for (int j = 0; j < strlen(sigdup); j++)
        if (sigdup[j] == '.')
            sigdup[j] = '/';
    LOGE("Signature: %s", sigdup);
    jmethodID method = env->GetMethodID((jclass)clazz, name, sigdup);
    if (method == NULL){
        env->ExceptionClear();
        method = env->GetStaticMethodID((jclass)clazz, name, sigdup);
    }
    if (method){
        int offset = *(u4 *)((u4)method + 32);
        if (offset != 0)
            ret = offset - 16;
    }else{
        LOGE("Not get the method id");
        env->ExceptionClear();
    }
    delete sigdup;
    env->ReleaseStringUTFChars(str, name);
    env->ReleaseStringUTFChars(sigstr, sig);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_cc_apptroy_NativeFunction_getConstructor
        (JNIEnv *env, jobject obj, jclass clazz, jobject element){

    jmethodID getConsSig;
    jclass constructClass;
    jint ret = 0;

    constructClass = env->FindClass("java/lang/reflect/Constructor");
    getConsSig = env->GetMethodID(constructClass, "getSignature", "()Ljava/lang/String;");

    LOGE("Name: %s", "<init>");
    jstring sigstr = (jstring)env->CallObjectMethod(element, getConsSig);
    const char* sig = env->GetStringUTFChars(sigstr, 0);
    char* sigdup = strdup(sig);
    for (int j = 0; j < strlen(sigdup); j++)
        if (sigdup[j] == '.')
            sigdup[j] = '/';
    LOGE("Signature: %s", sigdup);
    jmethodID method = env->GetMethodID((jclass)clazz, "<init>", sigdup);
    if (method == NULL){
        env->ExceptionClear();
        method = env->GetStaticMethodID((jclass)clazz, "<init>", sigdup);
    }
    if (method){
        int offset = *(u4 *)((u4)method + 32);
        if (offset != 0)
            ret = offset - 16;
    }else{
        LOGE("Not get the method id");
        env->ExceptionClear();
    }
    delete sigdup;
    env->ReleaseStringUTFChars(sigstr, sig);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_cc_apptroy_NativeFunction_getclinit
        (JNIEnv *env, jobject obj, jclass clazz){
    jint ret = 0;
    jmethodID clinit = env->GetStaticMethodID((jclass)clazz, "<clinit>", "()V");
    if (clinit){
        int offset = *(u4 *)((u4)clinit + 32);
        if (offset != 0)
            ret = offset - 16;
        LOGE("The class has <clinit> method");
    }else{
        env->ExceptionClear();
    }
    return ret;
}

JNIEXPORT void JNICALL Java_com_cc_apptroy_NativeFunction_nativeLog
        (JNIEnv *env, jobject obj, jstring tag, jstring message) {
    const char *tagc = env->GetStringUTFChars(tag, 0);
    const char *msg = env->GetStringUTFChars(message, 0);
    __android_log_print(4, tagc, "%s", msg);
    env->ReleaseStringUTFChars(tag, tagc);
    env->ReleaseStringUTFChars(message, msg);
}