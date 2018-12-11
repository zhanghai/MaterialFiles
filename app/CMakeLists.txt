cmake_minimum_required(VERSION 3.4.1)

find_library(log_LIBRARY log)

add_library(syscalls SHARED src/main/native/syscalls.c)
target_link_libraries(syscalls ${log_LIBRARY})

add_executable(libfs.so src/main/native/fs.c)
