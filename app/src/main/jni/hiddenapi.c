/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

#include <jni.h>

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    void *vmEnv = NULL;
    if ((*vm)->GetEnv(vm, &vmEnv, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    JNIEnv *env = vmEnv;
    jclass vmRuntimeClass = (*env)->FindClass(env, "dalvik/system/VMRuntime");
    if (!vmRuntimeClass) {
        return JNI_ERR;
    }
    jmethodID vmRuntimeGetRuntimeMethod = (*env)->GetStaticMethodID(env, vmRuntimeClass,
            "getRuntime", "()Ldalvik/system/VMRuntime;");
    if (!vmRuntimeGetRuntimeMethod) {
        (*env)->DeleteLocalRef(env, vmRuntimeClass);
        return JNI_ERR;
    }
    jobject vmRuntime = (*env)->CallStaticObjectMethod(env, vmRuntimeClass,
            vmRuntimeGetRuntimeMethod);
    if (!vmRuntime) {
        (*env)->DeleteLocalRef(env, vmRuntimeClass);
        return JNI_ERR;
    }
    jmethodID vmRuntimeSetHiddenApiExemptionsMethod = (*env)->GetMethodID(env, vmRuntimeClass,
            "setHiddenApiExemptions", "([Ljava/lang/String;)V");
    if (!vmRuntimeSetHiddenApiExemptionsMethod) {
        (*env)->DeleteLocalRef(env, vmRuntime);
        (*env)->DeleteLocalRef(env, vmRuntimeClass);
        return JNI_ERR;
    }
    jclass stringClass = (*env)->FindClass(env, "java/lang/String");
    if (!stringClass) {
        (*env)->DeleteLocalRef(env, vmRuntime);
        (*env)->DeleteLocalRef(env, vmRuntimeClass);
        return JNI_ERR;
    }
    jstring signaturePrefix = (*env)->NewStringUTF(env, "");
    if (!signaturePrefix) {
        (*env)->DeleteLocalRef(env, stringClass);
        (*env)->DeleteLocalRef(env, vmRuntime);
        (*env)->DeleteLocalRef(env, vmRuntimeClass);
        return JNI_ERR;
    }
    jobjectArray signaturePrefixes = (*env)->NewObjectArray(env, 1, stringClass, signaturePrefix);
    (*env)->DeleteLocalRef(env, signaturePrefix);
    (*env)->DeleteLocalRef(env, stringClass);
    if (!signaturePrefixes) {
        (*env)->DeleteLocalRef(env, vmRuntime);
        (*env)->DeleteLocalRef(env, vmRuntimeClass);
    }
    (*env)->CallVoidMethod(env, vmRuntime, vmRuntimeSetHiddenApiExemptionsMethod,
                           signaturePrefixes);
    (*env)->DeleteLocalRef(env, signaturePrefixes);
    (*env)->DeleteLocalRef(env, vmRuntime);
    (*env)->DeleteLocalRef(env, vmRuntimeClass);
    return JNI_VERSION_1_6;
}
