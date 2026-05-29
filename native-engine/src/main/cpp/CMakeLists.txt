cmake_minimum_required(VERSION 3.22.1)
project(native_engine)

add_library(
    native_engine
    SHARED
    native_engine.cpp
    partition_analyzer.cpp
)

find_library(log-lib log)

target_link_libraries(native_engine ${log-lib})
