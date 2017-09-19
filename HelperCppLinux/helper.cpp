#include <cstdlib>
#include <iostream>
#include <jni.h>
#include <map>

#include "helper.h"

using namespace std;

namespace{
    bool checkAdminRights(){
        int counter = -1;
        string filename;

        //Make sure file does not exist.
        FILE *checkFileExists = nullptr;
        do{
            if(checkFileExists){
                fclose(checkFileExists);
            }
            counter++;
            filename = "/checkAdminRights" + to_string(counter) + ".txt";
            checkFileExists = fopen(filename.c_str(), "r");
        } while(checkFileExists);

        //Check write permissions
        FILE *checkAdminRights = fopen(filename.c_str(), "a+");
        bool hasAdminRights = checkAdminRights != nullptr;

        //Delete file
        if(hasAdminRights){
            fclose(checkAdminRights);
            remove(filename.c_str());
        }

        return hasAdminRights;
    }

    const map<string, string> commands = {
        //NOTE: The option for the message to display has to be the last one
        { "gksudo", "-D" },
        { "kdesudo", "-n -d --comment" }
    };

    string getGraphicalSudoCommand(string message){
        for(map<string, string>::const_iterator it = commands.begin(); it != commands.end(); it++){
            int exitCode = system(string(it->first).append(" -v 2>/dev/null >/dev/null").c_str());
            if(!exitCode){
                //FIXME Think about injection
                return string(it->first).append(" ").append(it->second).append(" \"").append(message).append("\"");
            }
        }
    }
}

jobject Java_bayern_steinbrecher_green3_helper_Helper_00024HelperAction_install
(JNIEnv *jnienv, jclass clazz, jobject object, jstring comment){
    cout << "Install" << endl;
    system("sudo echo HA");
}

jobject Java_bayern_steinbrecher_green3_helper_Helper_00024HelperAction_uninstall
(JNIEnv *jnienv, jclass clazz, jobject object, jstring comment){
    cout << "Uninstall" << endl;
}

int main(int argc, char **argv){
    string command = getGraphicalSudoCommand("my own message");
    cout << command.c_str() << endl;
    system(command.append(" echo HA").c_str());
    return 0;
}
