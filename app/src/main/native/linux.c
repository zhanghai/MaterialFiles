#include <errno.h>
#include <stdlib.h>
#include <string.h>

#include <grp.h>
#include <pwd.h>
#include <sys/types.h>
#include <unistd.h>

#include <jni.h>

#include <android/log.h>

#define ALOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define LOG_TAG "Linux"

static jclass findClass(JNIEnv *env, const char *name) {
    jclass localClass = (*env)->FindClass(env, name);
    jclass result = (*env)->NewGlobalRef(env, localClass);
    (*env)->DeleteLocalRef(env, localClass);
    if (result == NULL) {
        ALOGE("failed to find class '%s'", name);
        abort();
    }
    return result;
}

static jclass getErrnoExceptionClass(JNIEnv *env) {
    static jclass errnoExceptionClass = NULL;
    if (!errnoExceptionClass) {
        errnoExceptionClass = findClass(env, "android/system/ErrnoException");
    }
    return errnoExceptionClass;
}

static jclass getStringClass(JNIEnv *env) {
    static jclass stringClass = NULL;
    if (!stringClass) {
        stringClass = findClass(env, "java/lang/String");
    }
    return stringClass;
}

static jclass getStructGroupClass(JNIEnv *env) {
    static jclass structGroupClass = NULL;
    if (!structGroupClass) {
        structGroupClass = findClass(env,
                                     "me/zhanghai/android/materialfilemanager/jni/StructGroup");
    }
    return structGroupClass;
}

static jclass getStructPasswdClass(JNIEnv *env) {
    static jclass structPasswdClass = NULL;
    if (!structPasswdClass) {
        structPasswdClass = findClass(env,
                                      "me/zhanghai/android/materialfilemanager/jni/StructPasswd");
    }
    return structPasswdClass;
}

static void throwException(JNIEnv *env, jclass exceptionClass, jmethodID constructor3,
                           jmethodID constructor2, const char *functionName, int error) {
    jthrowable cause = NULL;
    if ((*env)->ExceptionCheck(env)) {
        cause = (*env)->ExceptionOccurred(env);
        (*env)->ExceptionClear(env);
    }
    jstring detailMessage = (*env)->NewStringUTF(env, functionName);
    if (detailMessage == NULL) {
        // Not really much we can do here. We're probably dead in the water,
        // but let's try to stumble on...
        (*env)->ExceptionClear(env);
    }
    jobject exception;
    if (cause != NULL) {
        exception = (*env)->NewObject(env, exceptionClass, constructor3, detailMessage, error,
                                      cause);
    } else {
        exception = (*env)->NewObject(env, exceptionClass, constructor2, detailMessage, error);
    }
    (*env)->Throw(env, exception);
    (*env)->DeleteLocalRef(env, detailMessage);
}

static void throwErrnoException(JNIEnv* env, const char* functionName) {
    int error = errno;
    static jmethodID constructor3 = NULL;
    if (!constructor3) {
        constructor3 = (*env)->GetMethodID(env, getErrnoExceptionClass(env), "<init>",
                                    "(Ljava/lang/String;ILjava/lang/Throwable;)V");
    }
    static jmethodID constructor2 = NULL;
    if (!constructor2) {
        constructor2 = (*env)->GetMethodID(env, getErrnoExceptionClass(env), "<init>",
                                    "(Ljava/lang/String;I)V");
    }
    throwException(env, getErrnoExceptionClass(env), constructor3, constructor2, functionName,
                   error);
}

static jobject makeStructPasswd(JNIEnv* env, const struct passwd *passwd) {
    static jmethodID constructor;
    if (!constructor) {
        constructor = (*env)->GetMethodID(
                env, getStructPasswdClass(env), "<init>",
                "(Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    }
    if (!constructor) {
        return NULL;
    }
    jstring pw_name;
    if (passwd->pw_name) {
        pw_name = (*env)->NewStringUTF(env, passwd->pw_name);
        if (!pw_name) {
            return NULL;
        }
    } else {
        pw_name = NULL;
    }
    jint pw_uid = passwd->pw_uid;
    jint pw_gid = passwd->pw_gid;
#ifdef __LP64__
    jstring pw_gecos;
    if (passwd->pw_gecos) {
        pw_gecos = (*env)->NewStringUTF(env, passwd->pw_gecos);
        if (!pw_gecos) {
            return NULL;
        }
    } else {
        pw_gecos = NULL;
    }
#else
    jstring pw_gecos = NULL;
#endif
    jstring pw_dir;
    if (passwd->pw_dir) {
        pw_dir = (*env)->NewStringUTF(env, passwd->pw_dir);
        if (!pw_dir) {
            return NULL;
        }
    } else {
        pw_dir = NULL;
    }
    jstring pw_shell;
    if (passwd->pw_shell) {
        pw_shell = (*env)->NewStringUTF(env, passwd->pw_shell);
        if (!pw_shell) {
            return NULL;
        }
    } else {
        pw_shell = NULL;
    }
    return (*env)->NewObject(env, getStructPasswdClass(env), constructor, pw_name, pw_uid, pw_gid,
                             pw_gecos, pw_dir, pw_shell);
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_materialfilemanager_jni_Linux_getpwnam(JNIEnv *env, jclass clazz,
                                                                jstring javaName) {
    const char *name = (*env)->GetStringUTFChars(env, javaName, NULL);
    size_t bufferSize = (size_t) sysconf(_SC_GETPW_R_SIZE_MAX);
    if (bufferSize == -1) {
        // See `man 3 getpwnam`
        bufferSize = 16384;
    }
    char buffer[bufferSize];
    struct passwd passwd;
    struct passwd *result;
    errno = getpwnam_r(name, &passwd, buffer, bufferSize, &result);
    (*env)->ReleaseStringUTFChars(env, javaName, name);
    if (errno) {
        throwErrnoException(env, "getpwnam_r");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return makeStructPasswd(env, result);
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_materialfilemanager_jni_Linux_getpwuid(JNIEnv *env, jclass clazz,
                                                                jint javaUid) {
    uid_t uid = (uid_t) javaUid;
    size_t bufferSize = (size_t) sysconf(_SC_GETPW_R_SIZE_MAX);
    if (bufferSize == -1) {
        // See `man 3 getpwuid`
        bufferSize = 16384;
    }
    char buffer[bufferSize];
    struct passwd passwd;
    struct passwd *result;
    errno = getpwuid_r(uid, &passwd, buffer, bufferSize, &result);
    if (errno) {
        throwErrnoException(env, "getpwnam_r");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return makeStructPasswd(env, result);
}

static jobject makeStructGroup(JNIEnv* env, const struct group *group) {
    static jmethodID constructor;
    if (!constructor) {
        constructor = (*env)->GetMethodID(
                env, getStructGroupClass(env), "<init>",
                "(Ljava/lang/String;Ljava/lang/String;I[Ljava/lang/String;)V");
    }
    if (!constructor) {
        return NULL;
    }
    jstring gr_name;
    if (group->gr_name) {
        gr_name = (*env)->NewStringUTF(env, group->gr_name);
        if (!gr_name) {
            return NULL;
        }
    } else {
        gr_name = NULL;
    }
    jstring gr_passwd;
    if (group->gr_passwd) {
        gr_passwd = (*env)->NewStringUTF(env, group->gr_passwd);
        if (!gr_passwd) {
            return NULL;
        }
    } else {
        gr_passwd = NULL;
    }
    jint gr_gid = group->gr_gid;
    jobjectArray gr_mem;
    if (group->gr_mem) {
        jsize gr_memSize = 0;
        for (char **gr_memIterator = group->gr_mem; *gr_memIterator; ++gr_memIterator) {
            ++gr_memSize;
        };
        gr_mem = (*env)->NewObjectArray(env, gr_memSize, getStringClass(env), NULL);
        if (!gr_mem) {
            return NULL;
        }
        jsize gr_memIndex = 0;
        for (char **gr_memIterator = group->gr_mem; *gr_memIterator; ++gr_memIterator,
                ++gr_memIndex) {
            jstring gr_memElement = (*env)->NewStringUTF(env, *gr_memIterator);
            if (!gr_memElement) {
                return NULL;
            }
            (*env)->SetObjectArrayElement(env, gr_mem, gr_memIndex, gr_memElement);
            (*env)->DeleteLocalRef(env, gr_memElement);
        };
    } else {
        gr_mem = NULL;
    }
    return (*env)->NewObject(env, getStructGroupClass(env), constructor, gr_name, gr_passwd, gr_gid,
                             gr_mem);
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_materialfilemanager_jni_Linux_getgrnam(JNIEnv *env, jclass clazz,
                                                                jstring javaName) {
#if __ANDROID_API__ >= 24
    const char *name = (*env)->GetStringUTFChars(env, javaName, NULL);
    size_t bufferSize = (size_t) sysconf(_SC_GETGR_R_SIZE_MAX);
    if (bufferSize == -1) {
        // See `man 3 getpwnam`
        bufferSize = 16384;
    }
    char buffer[bufferSize];
    struct group group;
    struct group *result;
    errno = getgrnam_r(name, &group, buffer, bufferSize, &result);
    (*env)->ReleaseStringUTFChars(env, javaName, name);
    if (errno) {
        throwErrnoException(env, "getgrnam_r");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return makeStructGroup(env, result);
#else
    const char *name = (*env)->GetStringUTFChars(env, javaName, NULL);
    errno = 0;
    struct group *result = getgrnam(name);
    (*env)->ReleaseStringUTFChars(env, javaName, name);
    if (errno) {
        throwErrnoException(env, "getgrnam");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return makeStructGroup(env, result);
#endif
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_materialfilemanager_jni_Linux_getgrgid(JNIEnv *env, jclass clazz,
                                                                jint javaGid) {
#if __ANDROID_API__ >= 24
    gid_t gid = (gid_t) javaGid;
    size_t bufferSize = (size_t) sysconf(_SC_GETGR_R_SIZE_MAX);
    if (bufferSize == -1) {
        // See `man 3 getpwnam`
        bufferSize = 16384;
    }
    char buffer[bufferSize];
    struct group group;
    struct group *result;
    errno = getgrgid_r(gid, &group, buffer, bufferSize, &result);
    if (errno) {
        throwErrnoException(env, "getgrgid_r");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return makeStructGroup(env, result);
#else
    gid_t gid = (gid_t) javaGid;
    errno = 0;
    struct group *result = getgrgid(gid);
    if (errno) {
        throwErrnoException(env, "getgrgid");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return makeStructGroup(env, result);
#endif
}
