cmake_minimum_required(VERSION 3.4.1)

find_library(log_LIBRARY log)

add_library(linux SHARED src/main/native/linux.c)
target_link_libraries(linux ${log_LIBRARY})

add_executable(libfs.so src/main/native/fs.c)
