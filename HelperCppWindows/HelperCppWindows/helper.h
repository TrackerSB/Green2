#include <jni.h>
/* Header for class bayern_steinbrecher_green2_helper_Helper_HelperAction */

#ifndef _Included_bayern_steinbrecher_green2_helper_Helper_HelperAction
#define _Included_bayern_steinbrecher_green2_helper_Helper_HelperAction
#ifdef __cplusplus
extern "C" {
#endif
    /*
     * Class:     bayern_steinbrecher_green2_helper_Helper_HelperAction
     * Method:    install
     * Signature: (Ljava/nio/file/Path;Ljava/lang/String;)Ljava/util/Optional;
     */
    JNIEXPORT jobject JNICALL Java_bayern_steinbrecher_green2_helper_Helper_00024HelperAction_install
    (JNIEnv *, jclass, jobject, jstring);

    /*
     * Class:     bayern_steinbrecher_green2_helper_Helper_HelperAction
     * Method:    uninstall
     * Signature: (Ljava/nio/file/Path;Ljava/lang/String;)Ljava/util/Optional;
     */
    JNIEXPORT jobject JNICALL Java_bayern_steinbrecher_green2_helper_Helper_00024HelperAction_uninstall
(JNIEnv *, jclass, jobject, jstring);

    __declspec (dllexport) void doActualUninstall();

#ifdef __cplusplus
}
#endif
#endif
