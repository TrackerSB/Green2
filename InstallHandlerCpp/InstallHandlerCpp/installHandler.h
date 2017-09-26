#include <jni.h>

#ifndef installHandler
#define installHandler
#ifdef __cplusplus
extern "C" {
#endif
    /*
     * Class:     bayern_steinbrecher_green2_installHandler_InstallHandler_Action
     * Method:    install
     * Signature: (Ljava/nio/file/Path;Ljava/lang/String;)Ljava/util/Optional;
     */
    JNIEXPORT jobject JNICALL Java_bayern_steinbrecher_green2_installHandler_InstallHandler_00024Action_install
    (JNIEnv *, jclass, jobject, jstring);

    /*
     * Class:     bayern_steinbrecher_green2_installHandler_InstallHandler_Action
     * Method:    uninstall
     * Signature: (Ljava/nio/file/Path;Ljava/lang/String;)Ljava/util/Optional;
     */
    JNIEXPORT jobject JNICALL Java_bayern_steinbrecher_green2_installHandler_InstallHandler_00024Action_uninstall
    (JNIEnv *, jclass, jobject, jstring);

#ifdef __cplusplus
}
#endif
#endif
