#include <errno.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>

#include <dirent.h>
#include <fcntl.h>
#include <grp.h>
#include <pwd.h>
#include <sys/sendfile.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/xattr.h>
#include <unistd.h>

#include <jni.h>

#include <android/log.h>

#define ALOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define LOG_TAG "Linux"

#undef TEMP_FAILURE_RETRY
#define TEMP_FAILURE_RETRY(exp) ({ \
    errno = 0; \
    __typeof__(exp) _rc; \
    do { \
        _rc = (exp); \
    } while (errno == EINTR); \
    _rc; })

#define TEMP_FAILURE_RETRY_R(exp) ({ \
    __typeof__(exp) _rc; \
    do { \
        _rc = (exp); \
    } while (_rc == EINTR); \
    _rc; })

#define TEMP_FAILURE_RETRY_V(exp) ({ \
    errno = 0; \
    do { \
        (exp); \
    } while (errno == EINTR); })

static jobjectArray newResizedObjectArray(JNIEnv *env, jobjectArray array, jclass elementClass,
                                          jsize length, jsize newLength) {
    jobjectArray newArray = (*env)->NewObjectArray(env, newLength, elementClass, NULL);
    if (!newArray) {
        return NULL;
    }
    for (jsize i = 0; i < length; ++i) {
        jobject element = (*env)->GetObjectArrayElement(env, array, i);
        (*env)->SetObjectArrayElement(env, newArray, i, element);
        (*env)->DeleteLocalRef(env, element);
    }
    return newArray;
}

static jclass findClass(JNIEnv *env, const char *name) {
    jclass localClass = (*env)->FindClass(env, name);
    jclass result = (*env)->NewGlobalRef(env, localClass);
    (*env)->DeleteLocalRef(env, localClass);
    if (!result) {
        ALOGE("failed to find class '%s'", name);
        abort();
    }
    return result;
}

static jfieldID findField(JNIEnv *env, jclass clazz, const char *name, const char *signature) {
    jfieldID field = (*env)->GetFieldID(env, clazz, name, signature);
    if (!field) {
        ALOGE("failed to find field '%s' '%s'", name, signature);
        abort();
    }
    return field;
}

static jmethodID findMethod(JNIEnv *env, jclass clazz, const char *name, const char *signature) {
    jmethodID method = (*env)->GetMethodID(env, clazz, name, signature);
    if (!method) {
        ALOGE("failed to find method '%s' '%s'", name, signature);
        abort();
    }
    return method;
}

static jclass getSyscallExceptionClass(JNIEnv *env) {
    static jclass syscallExceptionClass = NULL;
    if (!syscallExceptionClass) {
        syscallExceptionClass = findClass(env,
                "me/zhanghai/android/files/provider/linux/syscall/SyscallException");
    }
    return syscallExceptionClass;
}

static jclass getFileDescriptorClass(JNIEnv *env) {
    static jclass fileDescriptorClass = NULL;
    if (!fileDescriptorClass) {
        fileDescriptorClass = findClass(env, "java/io/FileDescriptor");
    }
    return fileDescriptorClass;
}

static jfieldID getFileDescriptorDescriptorField(JNIEnv *env) {
    static jclass fileDescriptorDescriptorField = NULL;
    if (!fileDescriptorDescriptorField) {
        fileDescriptorDescriptorField = findField(env, getFileDescriptorClass(env), "descriptor",
                                                  "I");
    }
    return fileDescriptorDescriptorField;
}

static jclass getInt64RefClass(JNIEnv *env) {
    static jclass int64RefClass = NULL;
    if (!int64RefClass) {
        int64RefClass = findClass(env, "android/system/Int64Ref");
    }
    return int64RefClass;
}

static jfieldID getInt64RefValueField(JNIEnv *env) {
    static jclass int64RefValueField = NULL;
    if (!int64RefValueField) {
        int64RefValueField = findField(env, getInt64RefClass(env), "value", "J");
    }
    return int64RefValueField;
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
                "me/zhanghai/android/files/provider/linux/syscall/StructGroup");
    }
    return structGroupClass;
}

static jclass getStructPasswdClass(JNIEnv *env) {
    static jclass structPasswdClass = NULL;
    if (!structPasswdClass) {
        structPasswdClass = findClass(env,
                "me/zhanghai/android/files/provider/linux/syscall/StructPasswd");
    }
    return structPasswdClass;
}

static jclass getStructStatClass(JNIEnv *env) {
    static jclass structStatClass = NULL;
    if (!structStatClass) {
        structStatClass = findClass(env,
                "me/zhanghai/android/files/provider/linux/syscall/StructStat");
    }
    return structStatClass;
}

static jclass getStructTimespecClass(JNIEnv *env) {
    static jclass structTimespecClass = NULL;
    if (!structTimespecClass) {
        structTimespecClass = findClass(env,
                "me/zhanghai/android/files/provider/linux/syscall/StructTimespec");
    }
    return structTimespecClass;
}

static jfieldID getStructTimespecTvSecField(JNIEnv *env) {
    static jclass structTimespecTvSecField = NULL;
    if (!structTimespecTvSecField) {
        structTimespecTvSecField = findField(env, getStructTimespecClass(env), "tv_sec", "J");
    }
    return structTimespecTvSecField;
}

static jfieldID getStructTimespecTvNsecField(JNIEnv *env) {
    static jclass structTimespecTvNsecField = NULL;
    if (!structTimespecTvNsecField) {
        structTimespecTvNsecField = findField(env, getStructTimespecClass(env), "tv_nsec", "J");
    }
    return structTimespecTvNsecField;
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

static void throwSyscallException(JNIEnv* env, const char* functionName) {
    int error = errno;
    static jmethodID constructor3 = NULL;
    if (!constructor3) {
        constructor3 = findMethod(env, getSyscallExceptionClass(env), "<init>",
                                  "(Ljava/lang/String;ILjava/lang/Throwable;)V");
    }
    static jmethodID constructor2 = NULL;
    if (!constructor2) {
        constructor2 = findMethod(env, getSyscallExceptionClass(env), "<init>",
                                  "(Ljava/lang/String;I)V");
    }
    throwException(env, getSyscallExceptionClass(env), constructor3, constructor2, functionName,
                   error);
}

static jobject makeStructGroup(JNIEnv* env, const struct group *group) {
    static jmethodID constructor = NULL;
    if (!constructor) {
        constructor = findMethod(env, getStructGroupClass(env), "<init>",
                                 "(Ljava/lang/String;Ljava/lang/String;I[Ljava/lang/String;)V");
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
        jsize gr_memLength = 0;
        for (char **gr_memIterator = group->gr_mem; *gr_memIterator; ++gr_memIterator) {
            ++gr_memLength;
        };
        gr_mem = (*env)->NewObjectArray(env, gr_memLength, getStringClass(env), NULL);
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
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_getgrgid(
        JNIEnv *env, jclass clazz, jint javaGid) {
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
    errno = TEMP_FAILURE_RETRY_R(getgrgid_r(gid, &group, buffer, bufferSize, &result));
    if (errno) {
        throwSyscallException(env, "getgrgid_r");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return makeStructGroup(env, result);
#else
    gid_t gid = (gid_t) javaGid;
    errno = 0;
    struct group *result = TEMP_FAILURE_RETRY(getgrgid(gid));
    if (errno) {
        throwSyscallException(env, "getgrgid");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return makeStructGroup(env, result);
#endif
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_getgrnam(
        JNIEnv *env, jclass clazz, jstring javaName) {
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
    errno = TEMP_FAILURE_RETRY_R(getgrnam_r(name, &group, buffer, bufferSize, &result));
    (*env)->ReleaseStringUTFChars(env, javaName, name);
    if (errno) {
        throwSyscallException(env, "getgrnam_r");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return makeStructGroup(env, result);
#else
    const char *name = (*env)->GetStringUTFChars(env, javaName, NULL);
    errno = 0;
    struct group *result = TEMP_FAILURE_RETRY(getgrnam(name));
    (*env)->ReleaseStringUTFChars(env, javaName, name);
    if (errno) {
        throwSyscallException(env, "getgrnam");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return makeStructGroup(env, result);
#endif
}

static jobject makeStructPasswd(JNIEnv* env, const struct passwd *passwd) {
    static jmethodID constructor = NULL;
    if (!constructor) {
        constructor = findMethod(env, getStructPasswdClass(env), "<init>",
                "(Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
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
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_getpwnam(
        JNIEnv *env, jclass clazz, jstring javaName) {
    const char *name = (*env)->GetStringUTFChars(env, javaName, NULL);
    size_t bufferSize = (size_t) sysconf(_SC_GETPW_R_SIZE_MAX);
    if (bufferSize == -1) {
        // See `man 3 getpwnam`
        bufferSize = 16384;
    }
    char buffer[bufferSize];
    struct passwd passwd;
    struct passwd *result;
    errno = TEMP_FAILURE_RETRY_R(getpwnam_r(name, &passwd, buffer, bufferSize, &result));
    (*env)->ReleaseStringUTFChars(env, javaName, name);
    if (errno) {
        throwSyscallException(env, "getpwnam_r");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return makeStructPasswd(env, result);
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_getpwuid(
        JNIEnv *env, jclass clazz, jint javaUid) {
    uid_t uid = (uid_t) javaUid;
    size_t bufferSize = (size_t) sysconf(_SC_GETPW_R_SIZE_MAX);
    if (bufferSize == -1) {
        // See `man 3 getpwuid`
        bufferSize = 16384;
    }
    char buffer[bufferSize];
    struct passwd passwd;
    struct passwd *result;
    errno = TEMP_FAILURE_RETRY_R(getpwuid_r(uid, &passwd, buffer, bufferSize, &result));
    if (errno) {
        throwSyscallException(env, "getpwnam_r");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return makeStructPasswd(env, result);
}

JNIEXPORT jbyteArray JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_lgetxattr(
        JNIEnv *env, jclass clazz, jstring javaPath, jstring javaName) {
    const char *path = (*env)->GetStringUTFChars(env, javaPath, NULL);
    const char *name = (*env)->GetStringUTFChars(env, javaName, NULL);
    jbyteArray javaValue = NULL;
    while (true) {
        errno = 0;
        size_t size = (size_t) TEMP_FAILURE_RETRY(lgetxattr(path, name, NULL, 0));
        if (errno) {
            break;
        }
        char value[size];
        errno = 0;
        TEMP_FAILURE_RETRY(lgetxattr(path, name, value, size));
        if (errno) {
            if (errno == ERANGE) {
                // Attribute value changed since our last call to lgetxattr(), try again.
                continue;
            }
            break;
        }
        jsize javaValueLength = (jsize) size;
        javaValue = (*env)->NewByteArray(env, javaValueLength);
        if (!javaValue) {
            break;
        }
        const jbyte *javaValueBuffer = (const jbyte *) value;
        (*env)->SetByteArrayRegion(env, javaValue, 0, javaValueLength, javaValueBuffer);
        break;
    }
    (*env)->ReleaseStringUTFChars(env, javaPath, path);
    (*env)->ReleaseStringUTFChars(env, javaName, name);
    if (errno) {
        throwSyscallException(env, "lgetxattr");
        return NULL;
    }
    return javaValue;
}

/// @see https://android.googlesource.com/platform/libcore/+/master/ojluni/src/main/native/UnixFileSystem_md.c
JNIEXPORT jobjectArray JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_listdir(
        JNIEnv *env, jclass clazz, jstring javaPath) {
    const char *path = (*env)->GetStringUTFChars(env, javaPath, NULL);
    errno = 0;
    DIR *dir = TEMP_FAILURE_RETRY(opendir(path));
    (*env)->ReleaseStringUTFChars(env, javaPath, path);
    if (errno) {
        throwSyscallException(env, "opendir");
        return NULL;
    }
    jsize javaNamesLength = 0;
    jsize javaNamesMaxLength = 16;
    jobjectArray javaNames = (*env)->NewObjectArray(env, javaNamesMaxLength, getStringClass(env),
            NULL);
    if (!javaNames) {
        int oldErrno = errno;
        errno = 0;
        TEMP_FAILURE_RETRY_V(closedir(dir));
        errno = oldErrno;
        return NULL;
    }
    while (true) {
        errno = 0;
        struct dirent64 *dirent = TEMP_FAILURE_RETRY(readdir64(dir));
        if (errno) {
            int oldErrno = errno;
            errno = 0;
            TEMP_FAILURE_RETRY_V(closedir(dir));
            errno = oldErrno;
            (*env)->DeleteLocalRef(env, javaNames);
            throwSyscallException(env, "readdir64");
            return NULL;
        }
        if (!dirent) {
            break;
        }
        char *name = dirent->d_name;
        if (!strcmp(name, ".") || !strcmp(name, "..")) {
            continue;
        }
        if (javaNamesLength == javaNamesMaxLength) {
            jobjectArray oldJavaNames = javaNames;
            javaNamesMaxLength *= 2;
            javaNames = newResizedObjectArray(env, oldJavaNames, getStringClass(env),
                                              javaNamesLength, javaNamesMaxLength);
            (*env)->DeleteLocalRef(env, oldJavaNames);
            if (!javaNames) {
                int oldErrno = errno;
                errno = 0;
                TEMP_FAILURE_RETRY_V(closedir(dir));
                errno = oldErrno;
                return NULL;
            }
        }
        jstring javaName = (*env)->NewStringUTF(env, name);
        if (!javaName) {
            int oldErrno = errno;
            errno = 0;
            TEMP_FAILURE_RETRY_V(closedir(dir));
            errno = oldErrno;
            (*env)->DeleteLocalRef(env, javaNames);
            return NULL;
        }
        (*env)->SetObjectArrayElement(env, javaNames, javaNamesLength, javaName);
        (*env)->DeleteLocalRef(env, javaName);
        ++javaNamesLength;
    }
    errno = 0;
    TEMP_FAILURE_RETRY_V(closedir(dir));
    if (errno) {
        (*env)->DeleteLocalRef(env, javaNames);
        throwSyscallException(env, "closedir");
        return NULL;
    }
    jobjectArray oldJavaNames = javaNames;
    javaNames = newResizedObjectArray(env, oldJavaNames, getStringClass(env), javaNamesLength,
                                      javaNamesLength);
    (*env)->DeleteLocalRef(env, oldJavaNames);
    if (!javaNames) {
        return NULL;
    }
    return javaNames;
}

JNIEXPORT jobjectArray JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_llistxattr(
        JNIEnv *env, jclass clazz, jstring javaPath) {
    const char *path = (*env)->GetStringUTFChars(env, javaPath, NULL);
    jobjectArray javaNames = NULL;
    while (true) {
        errno = 0;
        size_t size = (size_t) TEMP_FAILURE_RETRY(llistxattr(path, NULL, 0));
        if (errno) {
            break;
        }
        char names[size];
        errno = 0;
        TEMP_FAILURE_RETRY(llistxattr(path, names, size));
        if (errno) {
            if (errno == ERANGE) {
                // Attribute value changed since our last call to llistxattr(), try again.
                continue;
            }
            break;
        }
        jsize javaNamesLength = 0;
        for (char *nameStart = names, *namesEnd = names + size; ; ) {
            char *nameEnd = memchr(nameStart, '\0', namesEnd - nameStart);
            if (!nameEnd) {
                break;
            }
            ++javaNamesLength;
            nameStart = nameEnd + 1;
        }
        javaNames = (*env)->NewObjectArray(env, javaNamesLength, getStringClass(env), NULL);
        if (!javaNames) {
            break;
        }
        jsize nameIndex = 0;
        for (char *nameStart = names, *namesEnd = names + size; ; ++nameIndex) {
            char *nameEnd = memchr(nameStart, '\0', namesEnd - nameStart);
            if (!nameEnd) {
                break;
            }
            jstring javaName = (*env)->NewStringUTF(env, nameStart);
            if (!javaName) {
                (*env)->DeleteLocalRef(env, javaNames);
                javaNames = NULL;
                break;
            }
            (*env)->SetObjectArrayElement(env, javaNames, nameIndex, javaName);
            (*env)->DeleteLocalRef(env, javaName);
            nameStart = nameEnd + 1;
        }
        break;
    }
    (*env)->ReleaseStringUTFChars(env, javaPath, path);
    if (errno) {
        throwSyscallException(env, "llistxattr");
        return NULL;
    }
    return javaNames;
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_lsetxattr(
        JNIEnv *env, jclass clazz, jstring javaPath, jstring javaName, jbyteArray javaValue,
        jint javaFlags) {
    const char *path = (*env)->GetStringUTFChars(env, javaPath, NULL);
    const char *name = (*env)->GetStringUTFChars(env, javaName, NULL);
    jbyte *value = (*env)->GetByteArrayElements(env, javaValue, NULL);
    size_t size = (size_t) (*env)->GetArrayLength(env, javaValue);
    int flags = javaFlags;
    errno = 0;
    TEMP_FAILURE_RETRY(lsetxattr(path, name, value, size, flags));
    (*env)->ReleaseStringUTFChars(env, javaPath, path);
    (*env)->ReleaseStringUTFChars(env, javaName, name);
    (*env)->ReleaseByteArrayElements(env, javaValue, value, JNI_ABORT);
    if (errno) {
        throwSyscallException(env, "lsetxattr");
    }
}

static jobject makeStructTimespec(JNIEnv* env, const struct timespec *timespec) {
    static jmethodID constructor = NULL;
    if (!constructor) {
        constructor = findMethod(env, getStructTimespecClass(env), "<init>", "(JJ)V");
    }
    jlong tv_sec = timespec->tv_sec;
    jlong tv_nsec = timespec->tv_nsec;
    return (*env)->NewObject(env, getStructTimespecClass(env), constructor, tv_sec, tv_nsec);
}

static jobject makeStructStat(JNIEnv* env, const struct stat64 *stat) {
    static jmethodID constructor = NULL;
    if (!constructor) {
        constructor = findMethod(env, getStructStatClass(env), "<init>", "(JJIJIIJJJJ"
                "Lme/zhanghai/android/files/provider/linux/syscall/StructTimespec;"
                "Lme/zhanghai/android/files/provider/linux/syscall/StructTimespec;"
                "Lme/zhanghai/android/files/provider/linux/syscall/StructTimespec;)V");
    }
    jlong st_dev = stat->st_dev;
    jlong st_ino = stat->st_ino;
    jint st_mode = stat->st_mode;
    jlong st_nlink = stat->st_nlink;
    jint st_uid = stat->st_uid;
    jint st_gid = stat->st_gid;
    jlong st_rdev = stat->st_rdev;
    jlong st_size = stat->st_size;
    jlong st_blksize = stat->st_blksize;
    jlong st_blocks = stat->st_blocks;
    jobject st_atim = makeStructTimespec(env, &stat->st_atim);
    if (!st_atim) {
        return NULL;
    }
    jobject st_mtim = makeStructTimespec(env, &stat->st_mtim);
    if (!st_mtim) {
        return NULL;
    }
    jobject st_ctim = makeStructTimespec(env, &stat->st_ctim);
    if (!st_ctim) {
        return NULL;
    }
    return (*env)->NewObject(env, getStructStatClass(env), constructor, st_dev, st_ino, st_mode,
                             st_nlink, st_uid, st_gid, st_rdev, st_size, st_blksize, st_blocks,
                             st_atim, st_mtim, st_ctim);
}

static jobject doStat(JNIEnv *env, jstring javaPath, bool isLstat) {
    const char *path = (*env)->GetStringUTFChars(env, javaPath, NULL);
    struct stat64 stat;
    errno = 0;
    TEMP_FAILURE_RETRY((isLstat ? lstat64 : stat64)(path, &stat));
    (*env)->ReleaseStringUTFChars(env, javaPath, path);
    if (errno) {
        throwSyscallException(env, isLstat ? "lstat64" : "stat64");
        return NULL;
    }
    return makeStructStat(env, &stat);
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_lstat(
        JNIEnv *env, jclass clazz, jstring javaPath) {
    return doStat(env, javaPath, true);
}

static void readStructTimespec(JNIEnv *env, jobject javaTime, struct timespec *time) {
    time->tv_sec = (*env)->GetLongField(env, javaTime, getStructTimespecTvSecField(env));
    time->tv_nsec = (*env)->GetLongField(env, javaTime, getStructTimespecTvNsecField(env));
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_lutimens(
        JNIEnv *env, jclass clazz, jstring javaPath, jobjectArray javaTimes) {
    const char *path = (*env)->GetStringUTFChars(env, javaPath, NULL);
    size_t timesSize = (size_t) (*env)->GetArrayLength(env, javaTimes);
    struct timespec times[timesSize];
    for (size_t i = 0; i < timesSize; ++i) {
        jsize javaTimeIndex = (jsize) i;
        jobject javaTime = (*env)->GetObjectArrayElement(env, javaTimes, javaTimeIndex);
        readStructTimespec(env, javaTime, &times[i]);
        (*env)->DeleteLocalRef(env, javaTime);
    }
    errno = 0;
    TEMP_FAILURE_RETRY(utimensat(AT_FDCWD, path, times, AT_SYMLINK_NOFOLLOW));
    (*env)->ReleaseStringUTFChars(env, javaPath, path);
    if (errno) {
        throwSyscallException(env, "utimensat");
    }
}

JNIEXPORT jlong JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_sendfile(
        JNIEnv* env, jclass clazz, jobject javaOutFd, jobject javaInFd, jobject javaOffset,
        jlong javaCount) {
    int outFd = javaOutFd ? (*env)->GetIntField(env, javaOutFd, getFileDescriptorDescriptorField(
            env)) : -1;
    int inFd = javaInFd ? (*env)->GetIntField(env, javaInFd, getFileDescriptorDescriptorField(env))
                        : -1;
    off64_t offset = 0;
    off64_t* offsetPointer = NULL;
    if (javaOffset) {
        offset = (*env)->GetLongField(env, javaOffset, getInt64RefValueField(env));
        offsetPointer = &offset;
    }
    size_t count = (size_t) javaCount;
    errno = 0;
    jlong result = TEMP_FAILURE_RETRY(sendfile64(outFd, inFd, offsetPointer, count));
    if (errno) {
        throwSyscallException(env, "sendfile64");
        return result;
    }
    if (javaOffset) {
        (*env)->SetLongField(env, javaOffset, getInt64RefValueField(env), offset);
    }
    return result;
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_stat(
        JNIEnv *env, jclass clazz, jstring javaPath) {
    return doStat(env, javaPath, false);
}
