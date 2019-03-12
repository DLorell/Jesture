cmake_minimum_required(VERSION 3.0 FATAL_ERROR)

add_library( # Specifies the name of the library.
             HelloJNI

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             HelloJNI.cpp )