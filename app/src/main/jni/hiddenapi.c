/*
 * Copyright (c) 2022 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

#include <pthread.h>

#include <jni.h>

// See also
// https://androidreverse.wordpress.com/2020/05/02/android-api-restriction-bypass-for-all-android-versions/

struct SetHiddenApiExemptionArgs {
    JavaVM *vm;
    jobject signaturePrefixes;
    jboolean isSuccessful;
};

static void *setHiddenApiExemptions(void *arg) {
    struct SetHiddenApiExemptionArgs *args = arg;
    JavaVM *vm = args->vm;
    jobject signaturePrefixes = args->signaturePrefixes;
    jboolean *isSuccessful = &args->isSuccessful;
    *isSuccessful = JNI_FALSE;
    JNIEnv *env = NULL;
    if ((*vm)->AttachCurrentThread(vm, &env, NULL) != JNI_OK) {
        return NULL;
    }
    jclass vmRuntimeClass = (*env)->FindClass(env, "dalvik/system/VMRuntime");
    if (!vmRuntimeClass) {
        (*vm)->DetachCurrentThread(vm);
        return NULL;
    }
    jmethodID vmRuntimeGetRuntimeMethod = (*env)->GetStaticMethodID(env, vmRuntimeClass,
            "getRuntime", "()Ldalvik/system/VMRuntime;");
    if (!vmRuntimeGetRuntimeMethod) {
        (*env)->DeleteLocalRef(env, vmRuntimeClass);
        (*vm)->DetachCurrentThread(vm);
        return NULL;
    }
    jmethodID vmRuntimeSetHiddenApiExemptionsMethod = (*env)->GetMethodID(env, vmRuntimeClass,
            "setHiddenApiExemptions", "([Ljava/lang/String;)V");
    if (!vmRuntimeSetHiddenApiExemptionsMethod) {
        (*env)->DeleteLocalRef(env, vmRuntimeClass);
        (*vm)->DetachCurrentThread(vm);
        return NULL;
    }
    jobject vmRuntime = (*env)->CallStaticObjectMethod(env, vmRuntimeClass,
            vmRuntimeGetRuntimeMethod);
    if (!vmRuntime) {
        (*env)->DeleteLocalRef(env, vmRuntimeClass);
        (*vm)->DetachCurrentThread(vm);
        return NULL;
    }
    (*env)->CallVoidMethod(env, vmRuntime, vmRuntimeSetHiddenApiExemptionsMethod,
            signaturePrefixes);
    (*env)->DeleteLocalRef(env, vmRuntime);
    (*env)->DeleteLocalRef(env, vmRuntimeClass);
    (*vm)->DetachCurrentThread(vm);
    *isSuccessful = JNI_TRUE;
    return NULL;
}

JNIEXPORT jboolean JNICALL
Java_me_zhanghai_android_files_hiddenapi_HiddenApi_setExemptions(
        JNIEnv *env, jclass clazz, jobject signaturePrefixes) {
    JavaVM *vm = NULL;
    if ((*env)->GetJavaVM(env, &vm)) {
        return JNI_FALSE;
    }
    jobject globalSignaturePrefixes = (*env)->NewGlobalRef(env, signaturePrefixes);
    if (!globalSignaturePrefixes) {
        return JNI_FALSE;
    }
    struct SetHiddenApiExemptionArgs args = {vm, globalSignaturePrefixes, JNI_FALSE };
    pthread_t pthread;
    if (pthread_create(&pthread, NULL, setHiddenApiExemptions, &args)) {
        (*env)->DeleteGlobalRef(env, globalSignaturePrefixes);
        return JNI_FALSE;
    }
    if (pthread_join(pthread, NULL)) {
        (*env)->DeleteGlobalRef(env, globalSignaturePrefixes);
        return JNI_FALSE;
    }
    (*env)->DeleteGlobalRef(env, globalSignaturePrefixes);
    return args.isSuccessful;
}
