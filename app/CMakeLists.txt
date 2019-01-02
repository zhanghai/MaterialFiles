cmake_minimum_required(VERSION 3.4.1)

find_library(LOG_LIBRARY log)

add_library(syscalls SHARED src/main/native/syscalls.c)
target_link_libraries(syscalls ${LOG_LIBRARY})
