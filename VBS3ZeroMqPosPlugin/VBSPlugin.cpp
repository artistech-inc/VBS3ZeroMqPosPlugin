#include <windows.h>
#include "VBSPlugin.h"
#include "zmq.hpp"

#if DEBUG
#include <iostream>
#include <fstream>
#endif
#include <cstring>
#include <stdlib.h>
#include "Vbs3GetPos.pb.h"

#include <czmq.h>
#define HAVE_MODE_T

#include <pthread.h>
#include <semaphore.h>

using namespace std;

//https://resources.bisimulations.com/wiki/Category:VBS:_Scripting_Commands
// Command function declaration
typedef int (WINAPI * ExecuteCommandType)(const char *command, char *result, int resultLength);

// Command function definition
ExecuteCommandType ExecuteCommand = NULL;
zmq::socket_t *publisher;
zmq::context_t *context;

/* this function is run by the second thread */

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
	//ExecuteCommand("0 setOvercast 1", NULL, 0);
	char *pos = new char[255];

	VBS3::Position posBuffer;
    ExecuteCommand("getPos player", pos, 255);
	posBuffer.set_x(atof(strtok(pos, "[],")));
	posBuffer.set_y(atof(strtok(NULL, "[],")));
	posBuffer.set_z(atof(strtok(NULL, "[],")));
	posBuffer.set_deltat(deltaT);

    ExecuteCommand("getWorldCenter", pos, 255);
	posBuffer.set_worldcenterx(atof(strtok(pos, "[],")));
	posBuffer.set_worldcentery(atof(strtok(NULL, "[],")));

    //ExecuteCommand("eyePos player", pos, 255);
	//posBuffer.set_eyex(atof(strtok(pos, "[],")));
	//posBuffer.set_eyey(atof(strtok(NULL, "[],")));
	//posBuffer.set_eyez(atof(strtok(NULL, "[],")));

	ExecuteCommand("getDir player", pos, 255);
	posBuffer.set_dir(atof(pos));

	ExecuteCommand("getPlayerUID player", pos, 255);
	posBuffer.set_id(pos);

	delete [] pos;

	if (posBuffer.x() > 0.0 ||
		posBuffer.y() > 0.0 ||
		posBuffer.z() > 0.0 ||
		posBuffer.dir() > 0.0)
	{
		int size = posBuffer.ByteSize();
		void *data = malloc(size);
		posBuffer.SerializeToArray(data, size);
		publisher->send(data, size);
		free(data);

#if DEBUG
		ofstream file;
		file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
		file << "Protocol Buffer: " << endl << posBuffer.DebugString();
		file.close();
#endif
	}
}

// This function will be executed every time the script in the engine calls the script function "pluginFunction"
// We can be sure in this function the ExecuteCommand registering was already done.
// Note that the plugin takes responsibility for allocating and deleting the returned string
VBSPLUGIN_EXPORT const char* WINAPI PluginFunction(const char *input)
{
	static const char result[]="";
	return result;
}

// DllMain
BOOL WINAPI DllMain(HINSTANCE hDll, DWORD fdwReason, LPVOID lpvReserved)
{
#if DEBUG
	ofstream file;
	file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
#endif
	switch(fdwReason)
	{
		case DLL_PROCESS_ATTACH:
#if DEBUG
			file << "Called DllMain with DLL_PROCESS_ATTACH" << endl;
			file.flush();
#endif
			context = new zmq::context_t(1);
			publisher = new zmq::socket_t(*context, ZMQ_PUB);
			publisher->bind("tcp://*:5551");
			//OutputDebugString("Called DllMain with DLL_PROCESS_ATTACH\n");
		break;
		case DLL_PROCESS_DETACH:
#if DEBUG
			file << "Called DllMain with DLL_PROCESS_DETACH" << endl;
			file.flush();
#endif
			//CANNOT CURRENTLY CLOSE W/O CRASH!
			//This is because ZeroMQ is meant to be called within a single thread (at least open/close)
			//and the VBS plugin is called by any indeterminate thread...
			publisher->close();
			//context->close();	// <-- causes hang if implemented
			delete publisher;
			//delete context;	// <-- causes hang if implemented
			//OutputDebugString("Called DllMain with DLL_PROCESS_DETACH\n");
		break;
		case DLL_THREAD_ATTACH:
			//file << "Called DllMain with DLL_THREAD_ATTACH";
			//OutputDebugString("Called DllMain with DLL_THREAD_ATTACH\n");
		break;
		case DLL_THREAD_DETACH:
			//file << "Called DllMain with DLL_THREAD_DETACH";
			//OutputDebugString("Called DllMain with DLL_THREAD_DETACH\n");
		break;
	}
#if DEBUG
	file.close();
#endif
	return TRUE;
}
