#include <windows.h>
#include "VBSPlugin.h"

#include "zmq.h"

int hwclient() {
    printf ("Connecting to hello world server…\n");
    void *context = zmq_ctx_new ();
    void *requester = zmq_socket (context, ZMQ_REQ);
    zmq_connect (requester, "tcp://localhost:5555");

    int request_nbr;
    for (request_nbr = 0; request_nbr != 10; request_nbr++) {
        char buffer [10];
        printf ("Sending Hello %d...\n", request_nbr);
        zmq_send (requester, "Hello", 5, 0);
        zmq_recv (requester, buffer, 10, 0);
        printf ("Received World %d\n", request_nbr);
    }
    zmq_close (requester);
    zmq_ctx_destroy (context);
    return 0;
}

// Command function declaration
typedef int (WINAPI * ExecuteCommandType)(const char *command, char *result, int resultLength);

// Command function definition
ExecuteCommandType ExecuteCommand = NULL;

// Function that will register the ExecuteCommand function of the engine
VBSPLUGIN_EXPORT void WINAPI RegisterCommandFnc(void *executeCommandFnc)
{
  ExecuteCommand = (ExecuteCommandType)executeCommandFnc;
}

// This function will be executed every simulation step (every frame) and took a part in the simulation procedure.
// We can be sure in this function the ExecuteCommand registering was already done.
// deltaT is time in seconds since the last simulation step
VBSPLUGIN_EXPORT void WINAPI OnSimulationStep(float deltaT)
{
  //{ Sample code:
  ExecuteCommand("0 setOvercast 1", NULL, 0);
  //!}
}

// This function will be executed every time the script in the engine calls the script function "pluginFunction"
// We can be sure in this function the ExecuteCommand registering was already done.
// Note that the plugin takes responsibility for allocating and deleting the returned string
VBSPLUGIN_EXPORT const char* WINAPI PluginFunction(const char *input)
{
  //{ Sample code:
  static const char result[]="[1.0, 3.75]";
  return result;
  //!}
}

// DllMain
BOOL WINAPI DllMain(HINSTANCE hDll, DWORD fdwReason, LPVOID lpvReserved)
{
   switch(fdwReason)
   {
      case DLL_PROCESS_ATTACH:
         //OutputDebugString("Called DllMain with DLL_PROCESS_ATTACH\n");
         break;
      case DLL_PROCESS_DETACH:
         //OutputDebugString("Called DllMain with DLL_PROCESS_DETACH\n");
         break;
      case DLL_THREAD_ATTACH:
         //OutputDebugString("Called DllMain with DLL_THREAD_ATTACH\n");
         break;
      case DLL_THREAD_DETACH:
         //OutputDebugString("Called DllMain with DLL_THREAD_DETACH\n");
         break;
   }
   return TRUE;
}
