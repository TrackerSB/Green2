//#define __int64 long long
#include <jni.h>
#include <stdio.h>
#include "JNITest.h"

JNIEXPORT void JNICALL Java_jnitest_JNITest_printHelloWorld(JNIEnv *env, jclass obj) {
    printf("Hello World from C\n");
}