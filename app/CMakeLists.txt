cmake_minimum_required(VERSION 3.6)

project(MaterialFiles C)

add_library(hiddenapi SHARED src/main/jni/hiddenapi.c)

find_library(LOG_LIBRARY log)
add_library(syscall SHARED src/main/jni/syscall.c)
target_link_libraries(syscall ${LOG_LIBRARY})
