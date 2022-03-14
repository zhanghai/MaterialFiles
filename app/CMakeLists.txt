cmake_minimum_required(VERSION 3.6)

project(MaterialFiles C)

add_library(hiddenapi SHARED src/main/jni/hiddenapi.c)

find_library(LOG_LIBRARY log)
add_library(syscalls SHARED src/main/jni/syscalls.c)
target_link_libraries(syscalls ${LOG_LIBRARY})
