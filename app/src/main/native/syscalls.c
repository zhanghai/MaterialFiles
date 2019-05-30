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
#include <sys/statvfs.h>
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

#define LOG_TAG "syscalls"

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

static jclass findClass(JNIEnv *env, const char *name) {
    jclass localClass = (*env)->FindClass(env, name);
    jclass result = (*env)->NewGlobalRef(env, localClass);
    (*env)->DeleteLocalRef(env, localClass);
    if (!result) {
        ALOGE("Failed to find class '%s'", name);
        abort();
    }
    return result;
}

static jfieldID findField(JNIEnv *env, jclass clazz, const char *name, const char *signature) {
    jfieldID field = (*env)->GetFieldID(env, clazz, name, signature);
    if (!field) {
        ALOGE("Failed to find field '%s' '%s'", name, signature);
        abort();
    }
    return field;
}

static jmethodID findMethod(JNIEnv *env, jclass clazz, const char *name, const char *signature) {
    jmethodID method = (*env)->GetMethodID(env, clazz, name, signature);
    if (!method) {
        ALOGE("Failed to find method '%s' '%s'", name, signature);
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

static jclass getByteStringClass(JNIEnv *env) {
    static jclass byteStringClass = NULL;
    if (!byteStringClass) {
        byteStringClass = findClass(env, "me/zhanghai/android/files/provider/common/ByteString");
    }
    return byteStringClass;
}

static jfieldID getByteStringBytesField(JNIEnv *env) {
    static jclass byteStringBytesField = NULL;
    if (!byteStringBytesField) {
        byteStringBytesField = findField(env, getByteStringClass(env), "mBytes", "[B");
    }
    return byteStringBytesField;
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

static jclass getStructDirentClass(JNIEnv *env) {
    static jclass structStatClass = NULL;
    if (!structStatClass) {
        structStatClass = findClass(env,
                "me/zhanghai/android/files/provider/linux/syscall/StructDirent");
    }
    return structStatClass;
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

static jclass getStructStatVfsClass(JNIEnv *env) {
    static jclass structStatVfsClass = NULL;
    if (!structStatVfsClass) {
        structStatVfsClass = findClass(env, "android/system/StructStatVfs");
    }
    return structStatVfsClass;
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
    if (!detailMessage) {
        // Not really much we can do here. We're probably dead in the water,
        // but let's try to stumble on...
        (*env)->ExceptionClear(env);
    }
    jobject exception;
    if (cause) {
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

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_closedir(
        JNIEnv *env, jclass clazz, jlong javaDir) {
    DIR *dir = (DIR *) javaDir;
    TEMP_FAILURE_RETRY(closedir(dir));
    if (errno) {
        throwSyscallException(env, "closedir");
    }
}

JNIEXPORT jint JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_errno(
        JNIEnv *env, jclass clazz) {
    return errno;
}

static char *mallocStringFromByteString(JNIEnv *env, jobject javaByteString) {
    jbyteArray javaBytes = (*env)->GetObjectField(env, javaByteString, getByteStringBytesField(
            env));
    void *bytes = (*env)->GetByteArrayElements(env, javaBytes, NULL);
    jsize javaLength = (*env)->GetArrayLength(env, javaBytes);
    size_t length = (size_t) javaLength;
    char *string = malloc(length + 1);
    memcpy(string, bytes, length);
    (*env)->ReleaseByteArrayElements(env, javaBytes, bytes, JNI_ABORT);
    (*env)->DeleteLocalRef(env, javaBytes);
    string[length] = '\0';
    return string;
}

static jobject newByteString(JNIEnv *env, const void *bytes, size_t length) {
    static jmethodID constructor = NULL;
    if (!constructor) {
        constructor = findMethod(env, getByteStringClass(env), "<init>", "([BZ)V");
    }
    jsize javaLength = (jsize) length;
    jbyteArray javaBytes = (*env)->NewByteArray(env, javaLength);
    if (!javaBytes) {
        return NULL;
    }
    (*env)->SetByteArrayRegion(env, javaBytes, 0, javaLength, bytes);
    jobject javaByteString = (*env)->NewObject(env, getByteStringClass(env), constructor, javaBytes,
            JNI_FALSE);
    (*env)->DeleteLocalRef(env, javaBytes);
    return javaByteString;
}

static jobject newByteStringFromString(JNIEnv *env, const char *string) {
    return newByteString(env, string, strlen(string));
}

JNIEXPORT jboolean JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_access(
        JNIEnv *env, jclass clazz, jobject javaPath, jint javaMode) {
    char *path = mallocStringFromByteString(env, javaPath);
    int mode = javaMode;
    int result = TEMP_FAILURE_RETRY(access(path, mode));
    free(path);
    if (errno) {
        throwSyscallException(env, "access");
        return NULL;
    }
    bool accessible = result == 0;
    return (jboolean) accessible;
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_chmod(
        JNIEnv *env, jclass clazz, jobject javaPath, jint javaMode) {
    char *path = mallocStringFromByteString(env, javaPath);
    mode_t mode = (mode_t) javaMode;
    TEMP_FAILURE_RETRY_V(chmod(path, mode));
    free(path);
    if (errno) {
        throwSyscallException(env, "chmod");
    }
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_chown(
        JNIEnv *env, jclass clazz, jobject javaPath, jint javaUid, jint javaGid) {
    char *path = mallocStringFromByteString(env, javaPath);
    uid_t uid = (uid_t) javaUid;
    gid_t gid = (gid_t) javaGid;
    TEMP_FAILURE_RETRY_V(chown(path, uid, gid));
    free(path);
    if (errno) {
        throwSyscallException(env, "chown");
    }
}

static jobject newStructGroup(JNIEnv *env, const struct group *group) {
    static jmethodID constructor = NULL;
    if (!constructor) {
        constructor = findMethod(env, getStructGroupClass(env), "<init>",
                                 "(Lme/zhanghai/android/files/provider/common/ByteString;"
                                 "Lme/zhanghai/android/files/provider/common/ByteString;I"
                                 "[Lme/zhanghai/android/files/provider/common/ByteString;)V");
    }
    jobject gr_name;
    if (group->gr_name) {
        gr_name = newByteStringFromString(env, group->gr_name);
        if (!gr_name) {
            return NULL;
        }
    } else {
        gr_name = NULL;
    }
    jobject gr_passwd;
    if (group->gr_passwd) {
        gr_passwd = newByteStringFromString(env, group->gr_passwd);
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
        gr_mem = (*env)->NewObjectArray(env, gr_memLength, getByteStringClass(env), NULL);
        if (!gr_mem) {
            return NULL;
        }
        jsize gr_memIndex = 0;
        for (char **gr_memIterator = group->gr_mem; *gr_memIterator; ++gr_memIterator,
                ++gr_memIndex) {
            jobject gr_memElement = newByteStringFromString(env, *gr_memIterator);
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
    return newStructGroup(env, result);
#else
    gid_t gid = (gid_t) javaGid;
    struct group *result = TEMP_FAILURE_RETRY(getgrgid(gid));
    if (errno) {
        throwSyscallException(env, "getgrgid");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return newStructGroup(env, result);
#endif
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_getgrnam(
        JNIEnv *env, jclass clazz, jobject javaName) {
#if __ANDROID_API__ >= 24
    char *name = mallocStringFromByteString(env, javaName);
    size_t bufferSize = (size_t) sysconf(_SC_GETGR_R_SIZE_MAX);
    if (bufferSize == -1) {
        // See `man 3 getpwnam`
        bufferSize = 16384;
    }
    char buffer[bufferSize];
    struct group group;
    struct group *result;
    errno = TEMP_FAILURE_RETRY_R(getgrnam_r(name, &group, buffer, bufferSize, &result));
    free(name);
    if (errno) {
        throwSyscallException(env, "getgrnam_r");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return newStructGroup(env, result);
#else
    char *name = mallocStringFromByteString(env, javaName);
    struct group *result = TEMP_FAILURE_RETRY(getgrnam(name));
    free(name);
    if (errno) {
        throwSyscallException(env, "getgrnam");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return newStructGroup(env, result);
#endif
}

static jobject newStructPasswd(JNIEnv *env, const struct passwd *passwd) {
    static jmethodID constructor = NULL;
    if (!constructor) {
        constructor = findMethod(env, getStructPasswdClass(env), "<init>",
                "(Lme/zhanghai/android/files/provider/common/ByteString;II"
                "Lme/zhanghai/android/files/provider/common/ByteString;"
                "Lme/zhanghai/android/files/provider/common/ByteString;"
                "Lme/zhanghai/android/files/provider/common/ByteString;)V");
    }
    jobject pw_name;
    if (passwd->pw_name) {
        pw_name = newByteStringFromString(env, passwd->pw_name);
        if (!pw_name) {
            return NULL;
        }
    } else {
        pw_name = NULL;
    }
    jint pw_uid = passwd->pw_uid;
    jint pw_gid = passwd->pw_gid;
#ifdef __LP64__
    jobject pw_gecos;
    if (passwd->pw_gecos) {
        pw_gecos = newByteStringFromString(env, passwd->pw_gecos);
        if (!pw_gecos) {
            return NULL;
        }
    } else {
        pw_gecos = NULL;
    }
#else
    jobject pw_gecos = NULL;
#endif
    jobject pw_dir;
    if (passwd->pw_dir) {
        pw_dir = newByteStringFromString(env, passwd->pw_dir);
        if (!pw_dir) {
            return NULL;
        }
    } else {
        pw_dir = NULL;
    }
    jobject pw_shell;
    if (passwd->pw_shell) {
        pw_shell = newByteStringFromString(env, passwd->pw_shell);
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
        JNIEnv *env, jclass clazz, jobject javaName) {
    char *name = mallocStringFromByteString(env, javaName);
    size_t bufferSize = (size_t) sysconf(_SC_GETPW_R_SIZE_MAX);
    if (bufferSize == -1) {
        // See `man 3 getpwnam`
        bufferSize = 16384;
    }
    char buffer[bufferSize];
    struct passwd passwd;
    struct passwd *result;
    errno = TEMP_FAILURE_RETRY_R(getpwnam_r(name, &passwd, buffer, bufferSize, &result));
    free(name);
    if (errno) {
        throwSyscallException(env, "getpwnam_r");
        return NULL;
    }
    if (!result) {
        return NULL;
    }
    return newStructPasswd(env, result);
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
    return newStructPasswd(env, result);
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_lchown(
        JNIEnv *env, jclass clazz, jobject javaPath, jint javaUid, jint javaGid) {
    char *path = mallocStringFromByteString(env, javaPath);
    uid_t uid = (uid_t) javaUid;
    gid_t gid = (gid_t) javaGid;
    TEMP_FAILURE_RETRY_V(lchown(path, uid, gid));
    free(path);
    if (errno) {
        throwSyscallException(env, "lchown");
    }
}

JNIEXPORT jbyteArray JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_lgetxattr(
        JNIEnv *env, jclass clazz, jobject javaPath, jobject javaName) {
    char *path = mallocStringFromByteString(env, javaPath);
    char *name = mallocStringFromByteString(env, javaName);
    jbyteArray javaValue = NULL;
    while (true) {
        size_t size = (size_t) TEMP_FAILURE_RETRY(lgetxattr(path, name, NULL, 0));
        if (errno) {
            break;
        }
        char value[size];
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
    free(path);
    free(name);
    if (errno) {
        throwSyscallException(env, "lgetxattr");
        return NULL;
    }
    return javaValue;
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_link(
        JNIEnv *env, jclass clazz, jobject javaOldPath, jobject javaNewPath) {
    char *oldPath = mallocStringFromByteString(env, javaOldPath);
    char *newPath = mallocStringFromByteString(env, javaNewPath);
    TEMP_FAILURE_RETRY_V(link(oldPath, newPath));
    free(oldPath);
    free(newPath);
    if (errno) {
        throwSyscallException(env, "link");
    }
}

JNIEXPORT jobjectArray JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_llistxattr(
        JNIEnv *env, jclass clazz, jobject javaPath) {
    char *path = mallocStringFromByteString(env, javaPath);
    jobjectArray javaNames = NULL;
    while (true) {
        size_t size = (size_t) TEMP_FAILURE_RETRY(llistxattr(path, NULL, 0));
        if (errno) {
            break;
        }
        char names[size];
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
        javaNames = (*env)->NewObjectArray(env, javaNamesLength, getByteStringClass(env), NULL);
        if (!javaNames) {
            break;
        }
        jsize nameIndex = 0;
        for (char *nameStart = names, *namesEnd = names + size; ; ++nameIndex) {
            char *nameEnd = memchr(nameStart, '\0', namesEnd - nameStart);
            if (!nameEnd) {
                break;
            }
            jobject javaName = newByteStringFromString(env, nameStart);
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
    free(path);
    if (errno) {
        throwSyscallException(env, "llistxattr");
        return NULL;
    }
    return javaNames;
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_lsetxattr(
        JNIEnv *env, jclass clazz, jobject javaPath, jobject javaName, jbyteArray javaValue,
        jint javaFlags) {
    char *path = mallocStringFromByteString(env, javaPath);
    char *name = mallocStringFromByteString(env, javaName);
    jbyte *value = (*env)->GetByteArrayElements(env, javaValue, NULL);
    size_t size = (size_t) (*env)->GetArrayLength(env, javaValue);
    int flags = javaFlags;
    TEMP_FAILURE_RETRY(lsetxattr(path, name, value, size, flags));
    free(path);
    free(name);
    (*env)->ReleaseByteArrayElements(env, javaValue, value, JNI_ABORT);
    if (errno) {
        throwSyscallException(env, "lsetxattr");
    }
}

static jobject newStructTimespec(JNIEnv *env, const struct timespec *timespec) {
    static jmethodID constructor = NULL;
    if (!constructor) {
        constructor = findMethod(env, getStructTimespecClass(env), "<init>", "(JJ)V");
    }
    jlong tv_sec = timespec->tv_sec;
    jlong tv_nsec = timespec->tv_nsec;
    return (*env)->NewObject(env, getStructTimespecClass(env), constructor, tv_sec, tv_nsec);
}

static jobject newStructStat(JNIEnv *env, const struct stat64 *stat) {
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
    jobject st_atim = newStructTimespec(env, &stat->st_atim);
    if (!st_atim) {
        return NULL;
    }
    jobject st_mtim = newStructTimespec(env, &stat->st_mtim);
    if (!st_mtim) {
        return NULL;
    }
    jobject st_ctim = newStructTimespec(env, &stat->st_ctim);
    if (!st_ctim) {
        return NULL;
    }
    return (*env)->NewObject(env, getStructStatClass(env), constructor, st_dev, st_ino, st_mode,
                             st_nlink, st_uid, st_gid, st_rdev, st_size, st_blksize, st_blocks,
                             st_atim, st_mtim, st_ctim);
}

static jobject doStat(JNIEnv *env, jobject javaPath, bool isLstat) {
    char *path = mallocStringFromByteString(env, javaPath);
    struct stat64 stat;
    TEMP_FAILURE_RETRY((isLstat ? lstat64 : stat64)(path, &stat));
    free(path);
    if (errno) {
        throwSyscallException(env, isLstat ? "lstat64" : "stat64");
        return NULL;
    }
    return newStructStat(env, &stat);
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_lstat(
        JNIEnv *env, jclass clazz, jobject javaPath) {
    return doStat(env, javaPath, true);
}

static void readStructTimespec(JNIEnv *env, jobject javaTime, struct timespec *time) {
    time->tv_sec = (*env)->GetLongField(env, javaTime, getStructTimespecTvSecField(env));
    time->tv_nsec = (*env)->GetLongField(env, javaTime, getStructTimespecTvNsecField(env));
}

JNIEXPORT void JNICALL
doUtimens(JNIEnv *env, jobject javaPath, jobjectArray javaTimes, bool isLutimens) {
    char *path = mallocStringFromByteString(env, javaPath);
    size_t timesSize = (size_t) (*env)->GetArrayLength(env, javaTimes);
    struct timespec times[timesSize];
    for (size_t i = 0; i < timesSize; ++i) {
        jsize javaTimeIndex = (jsize) i;
        jobject javaTime = (*env)->GetObjectArrayElement(env, javaTimes, javaTimeIndex);
        readStructTimespec(env, javaTime, &times[i]);
        (*env)->DeleteLocalRef(env, javaTime);
    }
    TEMP_FAILURE_RETRY(utimensat(AT_FDCWD, path, times, isLutimens ? AT_SYMLINK_NOFOLLOW : 0));
    free(path);
    if (errno) {
        throwSyscallException(env, "utimensat");
    }
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_lutimens(
        JNIEnv *env, jclass clazz, jobject javaPath, jobjectArray javaTimes) {
    doUtimens(env, javaPath, javaTimes, true);
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_mkdir(
        JNIEnv *env, jclass clazz, jobject javaPath, jint javaMode) {
    char *path = mallocStringFromByteString(env, javaPath);
    mode_t mode = (mode_t) javaMode;
    TEMP_FAILURE_RETRY_V(mkdir(path, mode));
    free(path);
    if (errno) {
        throwSyscallException(env, "mkdir");
    }
}

static jobject newFileDescriptor(JNIEnv *env, int fd) {
    static jmethodID constructor = NULL;
    if (!constructor) {
        constructor = findMethod(env, getFileDescriptorClass(env), "<init>", "()V");
    }
    jobject javaFileDescriptor = (*env)->NewObject(env, getFileDescriptorClass(env), constructor);
    if (!javaFileDescriptor) {
        return NULL;
    }
    (*env)->SetIntField(env, javaFileDescriptor, getFileDescriptorDescriptorField(env), fd);
    return javaFileDescriptor;
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_open(
        JNIEnv *env, jclass clazz, jobject javaPath, jint javaFlags, jint javaMode) {
    char *path = mallocStringFromByteString(env, javaPath);
    int flags = javaFlags;
    mode_t mode = (mode_t) javaMode;
    int fd = TEMP_FAILURE_RETRY(open(path, flags, mode));
    free(path);
    if (errno) {
        throwSyscallException(env, "open");
        return NULL;
    }
    return newFileDescriptor(env, fd);
}

JNIEXPORT jlong JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_opendir(
        JNIEnv *env, jclass clazz, jobject javaPath) {
    char *path = mallocStringFromByteString(env, javaPath);
    DIR *dir = TEMP_FAILURE_RETRY(opendir(path));
    free(path);
    if (errno) {
        throwSyscallException(env, "opendir");
        return (jlong) NULL;
    }
    return (jlong) dir;
}

static jobject newStructDirent(JNIEnv *env, const struct dirent64 *dirent) {
    static jmethodID constructor = NULL;
    if (!constructor) {
        constructor = findMethod(env, getStructDirentClass(env), "<init>",
                                 "(JJIILme/zhanghai/android/files/provider/common/ByteString;)V");
    }
    jlong d_ino = dirent->d_ino;
    jlong d_off = dirent->d_off;
    jint d_reclen = dirent->d_reclen;
    jint d_type = dirent->d_type;
    jobject d_name = newByteStringFromString(env, dirent->d_name);
    if (!d_name) {
        return NULL;
    }
    return (*env)->NewObject(env, getStructDirentClass(env), constructor, d_ino, d_off, d_reclen,
                             d_type, d_name);
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_readdir(
        JNIEnv *env, jclass clazz, jlong javaDir) {
    DIR *dir = (DIR *) javaDir;
    struct dirent64 *dirent = TEMP_FAILURE_RETRY(readdir64(dir));
    if (errno) {
        throwSyscallException(env, "readdir64");
        return NULL;
    }
    if (!dirent) {
        return NULL;
    }
    return newStructDirent(env, dirent);
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_readlink(
        JNIEnv *env, jclass clazz, jobject javaPath) {
    char *path = mallocStringFromByteString(env, javaPath);
    size_t maxSize = PATH_MAX;
    jobject javaTarget = NULL;
    while (true) {
        char target[maxSize];
        size_t size = (size_t) TEMP_FAILURE_RETRY(readlink(path, target, maxSize));
        if (errno) {
            break;
        }
        if (size >= maxSize) {
            maxSize *= 2;
            continue;
        }
        javaTarget = newByteString(env, target, size);
        break;
    }
    free(path);
    if (errno) {
        throwSyscallException(env, "readlink");
        return NULL;
    }
    return javaTarget;
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_realpath(
        JNIEnv *env, jclass clazz, jobject javaPath) {
    char *path = mallocStringFromByteString(env, javaPath);
    char resolvedPath[PATH_MAX];
    TEMP_FAILURE_RETRY(realpath(path, resolvedPath));
    free(path);
    if (errno) {
        throwSyscallException(env, "realpath");
        return NULL;
    }
    return newByteStringFromString(env, resolvedPath);
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_remove(
        JNIEnv *env, jclass clazz, jobject javaPath) {
    char *path = mallocStringFromByteString(env, javaPath);
    TEMP_FAILURE_RETRY_V(remove(path));
    free(path);
    if (errno) {
        throwSyscallException(env, "remove");
    }
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_rename(
        JNIEnv *env, jclass clazz, jobject javaOldPath, jobject javaNewPath) {
    char *oldPath = mallocStringFromByteString(env, javaOldPath);
    char *newPath = mallocStringFromByteString(env, javaNewPath);
    TEMP_FAILURE_RETRY_V(rename(oldPath, newPath));
    free(oldPath);
    free(newPath);
    if (errno) {
        throwSyscallException(env, "rename");
    }
}

JNIEXPORT jlong JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_sendfile(
        JNIEnv* env, jclass clazz, jobject javaOutFd, jobject javaInFd, jobject javaOffset,
        jlong javaCount) {
    int outFd = (*env)->GetIntField(env, javaOutFd, getFileDescriptorDescriptorField(env));
    int inFd = (*env)->GetIntField(env, javaInFd, getFileDescriptorDescriptorField(env));
    off64_t offset = 0;
    off64_t* offsetPointer = NULL;
    if (javaOffset) {
        offset = (*env)->GetLongField(env, javaOffset, getInt64RefValueField(env));
        offsetPointer = &offset;
    }
    size_t count = (size_t) javaCount;
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
        JNIEnv *env, jclass clazz, jobject javaPath) {
    return doStat(env, javaPath, false);
}

static jobject newStructStatVfs(JNIEnv *env, const struct statvfs64 *statvfs) {
    static jmethodID constructor = NULL;
    if (!constructor) {
        constructor = findMethod(env, getStructStatVfsClass(env), "<init>", "(JJJJJJJJJJJ)V");
    }
    jlong f_bsize = statvfs->f_bsize;
    jlong f_frsize = statvfs->f_frsize;
    jlong f_blocks = statvfs->f_blocks;
    jlong f_bfree = statvfs->f_bfree;
    jlong f_bavail = statvfs->f_bavail;
    jlong f_files = statvfs->f_files;
    jlong f_ffree = statvfs->f_ffree;
    jlong f_favail = statvfs->f_favail;
    jlong f_fsid = statvfs->f_fsid;
    jlong f_flag = statvfs->f_flag;
    jlong f_namemax = statvfs->f_namemax;
    return (*env)->NewObject(env, getStructStatVfsClass(env), constructor, f_bsize, f_frsize,
                             f_blocks, f_bfree, f_bavail, f_files, f_ffree, f_favail, f_fsid,
                             f_flag, f_namemax);
}

JNIEXPORT jobject JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_statvfs(
        JNIEnv *env, jclass clazz, jobject javaPath) {
    char *path = mallocStringFromByteString(env, javaPath);
    struct statvfs64 statvfs;
    TEMP_FAILURE_RETRY(statvfs64(path, &statvfs));
    free(path);
    if (errno) {
        throwSyscallException(env, "statvfs64");
        return NULL;
    }
    return newStructStatVfs(env, &statvfs);
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_symlink(
        JNIEnv *env, jclass clazz, jobject javaTarget, jobject javaLinkPath) {
    char *target = mallocStringFromByteString(env, javaTarget);
    char *linkPath = mallocStringFromByteString(env, javaLinkPath);
    TEMP_FAILURE_RETRY_V(symlink(target, linkPath));
    free(target);
    free(linkPath);
    if (errno) {
        throwSyscallException(env, "symlink");
    }
}

JNIEXPORT void JNICALL
Java_me_zhanghai_android_files_provider_linux_syscall_Syscalls_utimens(
        JNIEnv *env, jclass clazz, jobject javaPath, jobjectArray javaTimes) {
    doUtimens(env, javaPath, javaTimes, false);
}
