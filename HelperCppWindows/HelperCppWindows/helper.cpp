#include <fstream>
#include <iostream>
#include <jni.h>
#include <ShlObj.h>
#include "helper.h"

#define MAX_PARAM 100

using namespace std;

namespace {
    string getPathOfThisDll() {
        char path[MAX_PARAM];
        HMODULE hm = NULL;

        if (!GetModuleHandleExA(GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS |
            GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
            (LPCSTR)&getPathOfThisDll, &hm)) {
            int ret = GetLastError();
            fprintf(stderr, "GetModuleHandle returned %d\n", ret);
        }
        GetModuleFileNameA(hm, path, sizeof(path));

        // path variable should now contain the full filepath to localFunc

        return path;
    }

    void callElevatedMethod(string name) {
        SHELLEXECUTEINFO shExInfo = { 0 };
        shExInfo.cbSize = sizeof(shExInfo);
        shExInfo.fMask = SEE_MASK_NOCLOSEPROCESS;
        shExInfo.hwnd = 0;
        shExInfo.lpVerb = L"runas";
        shExInfo.lpFile = L"rundll32";
        string argument = getPathOfThisDll().append(",").append(name);
        shExInfo.lpParameters = wstring(argument.begin(), argument.end()).c_str();
        shExInfo.lpDirectory = 0;
        shExInfo.nShow = SW_SHOW;
        shExInfo.hInstApp = 0;

        if (ShellExecuteEx(&shExInfo)) {
            WaitForSingleObject(shExInfo.hProcess, INFINITE);
            CloseHandle(shExInfo.hProcess);
        } else {
            cout << "Hä?!" << endl;
        }
    }

    bool checkAdminRights() {
        DWORD dwSize = 0;
        HANDLE hToken = NULL;
        bool bReturn = false;

        TOKEN_ELEVATION tokenInformation;

        if (OpenProcessToken(GetCurrentProcess(), TOKEN_QUERY, &hToken)) {
            if (GetTokenInformation(hToken, TokenElevation, &tokenInformation, sizeof(TOKEN_ELEVATION), &dwSize)) {
                bReturn = tokenInformation.TokenIsElevated;
            }

            CloseHandle(hToken);
        }
        cout << "isAdmin: " << bReturn << endl;
        return bReturn;
    }
}

void doActualUninstall() {
    if (checkAdminRights()) {
        cout << "Doing the actual uninstall" << endl;
        ofstream out("C:/Users/Stefan Huber/Desktop/haha.txt", ios_base::app);
        out << "Got you" << endl;
        out.close();
    } else {
        cout << "Doing the actual uninstall" << endl;
        ofstream out("C:/Users/Stefan Huber/Desktop/haha.txt", ios_base::app);
        out << "Didn´t get you" << endl;
        out.close();
        callElevatedMethod("doActualUninstall");
    }
}

void JNICALL Java_bayern_steinbrecher_green2_helper_Helper_00024HelperAction_install
(JNIEnv *jnienv, jclass clazz, jobject object) {
    if (checkAdminRights()) {
        cout << "Install" << endl;
    } else {
        cout << "Not installing: Missing admin rights" << endl;
    }
}

void JNICALL Java_bayern_steinbrecher_green2_helper_Helper_00024HelperAction_uninstall
(JNIEnv *jnienv, jclass clazz, jobject object) {
    cout << "Uninstall called" << endl;
    doActualUninstall();
}
