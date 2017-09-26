#include <fstream>
#include <iostream>
#include <ShlObj.h>
#include <string>

#include "installHandler.h"

using namespace std;

#if defined(_WIN32)
#define OS_WINDOWS
#elif defined(__linux__)
#define OS_LINUX
#else
cerr << "No known os macro is defined" << endl;
#endif

namespace {
#ifdef OS_WINDOWS
#define MAX_PARAM 100

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
#endif

#ifdef OS_LINUX
    const map<string, string> commands = {
        //NOTE: The option for the message to display has to be the last one
        { "gksudo", "-D" },
        { "kdesudo", "-n -d --comment" }
    };

    string getGraphicalSudoCommand(string message) {
        for (map<string, string>::const_iterator it = commands.begin(); it != commands.end(); it++) {
            int exitCode = system(string(it->first).append(" -v 2>/dev/null >/dev/null").c_str());
            if (!exitCode) {
                //FIXME Think about injection
                return string(it->first).append(" ").append(it->second).append(" \"").append(message).append("\"");
            }
        }
    }
#endif

    BOOL IsProcessElevated() {
#ifdef OS_WINDOWS
        //Based on https://code.msdn.microsoft.com/windowsdesktop/CppUACSelfElevation-5bfc52dd
        BOOL fIsElevated = FALSE;
        DWORD dwError = ERROR_SUCCESS;
        HANDLE hToken = NULL;

        // Open the primary access token of the process with TOKEN_QUERY.
        if (!OpenProcessToken(GetCurrentProcess(), TOKEN_QUERY, &hToken)) {
            dwError = GetLastError();
            //goto Cleanup;
        }

        // Retrieve token elevation information.
        TOKEN_ELEVATION elevation;
        DWORD dwSize;
        if (!GetTokenInformation(hToken, TokenElevation, &elevation,
            sizeof(elevation), &dwSize)) {
            // When the process is run on operating systems prior to Windows 
            // Vista, GetTokenInformation returns FALSE with the 
            // ERROR_INVALID_PARAMETER error code because TokenElevation is 
            // not supported on those operating systems.
            dwError = GetLastError();
            goto Cleanup;
        }

        fIsElevated = elevation.TokenIsElevated;

    Cleanup:
        // Centralized cleanup for all allocated resources.
        if (hToken) {
            CloseHandle(hToken);
            hToken = NULL;
        }

        // Throw the error if something failed in the function.
        if (ERROR_SUCCESS != dwError) {
            throw dwError;
        }

        return fIsElevated;
#endif
#ifdef OS_LINUX
        int counter = -1;
        string filename;

        //Make sure file does not exist.
        FILE *checkFileExists = nullptr;
        do {
            if (checkFileExists) {
                fclose(checkFileExists);
            }
            counter++;
            filename = "/checkAdminRights" + to_string(counter) + ".txt";
            checkFileExists = fopen(filename.c_str(), "r");
        } while (checkFileExists);

        //Check write permissions
        FILE *checkAdminRights = fopen(filename.c_str(), "a+");
        bool hasAdminRights = checkAdminRights != nullptr;

        //Delete file
        if (hasAdminRights) {
            fclose(checkAdminRights);
            remove(filename.c_str());
        }

        return hasAdminRights;
#endif
    }

    //Based on http://www.cplusplus.com/forum/windows/101207/
    void executeElevatedCommand(char *command) {
        if (IsProcessElevated()) {
            string commandString(command);
            ofstream file;
            file.open("C:/Users/Stefan Huber/Desktop/haha.log");
            file << command << endl;
            file.close();
            if (commandString.compare("install")) {
                cout << "install" << endl;
            } else if (commandString.compare("uninstall")) {
                cout << "uninstall" << endl;
            } else {
                cerr << "Unknown comamnd: " << command << endl;
            }
        } else {
#ifdef OS_WINDOWS
            wchar_t szPath[MAX_PATH];
            if (GetModuleFileName(NULL, szPath, ARRAYSIZE(szPath))) {
                // Launch itself as administrator.
                SHELLEXECUTEINFO sei = { sizeof(sei) };
                sei.lpVerb = L"runas";
                sei.lpFile = szPath;
                //sei.hwnd = hWnd;
                sei.nShow = SW_NORMAL;

                if (!ShellExecuteEx(&sei)) {
                    DWORD dwError = GetLastError();
                    if (dwError == ERROR_CANCELLED) {
                        // The user refused the elevation.
                        // Do nothing ...
                    }
                    cerr << dwError << endl;
                } else {
                    cout << "HIER" << endl;
                    //EndDialog(hWnd, TRUE);  // Quit itself
                }
            }
#endif
        }
    }
}

jobject JNICALL Java_bayern_steinbrecher_green2_installHandler_InstallHandler_00024Action_install
(JNIEnv *env, jclass clazz, jobject path, jstring message) {
    executeElevatedCommand("install");
}

JNIEXPORT jobject JNICALL Java_bayern_steinbrecher_green2_installHandler_InstallHandler_00024Action_uninstall
(JNIEnv *, jclass, jobject, jstring) {
    executeElevatedCommand("uninstall");
}