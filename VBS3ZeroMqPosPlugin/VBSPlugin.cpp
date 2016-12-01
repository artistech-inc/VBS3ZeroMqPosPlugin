#include <windows.h>
#include "VBSPlugin.h"

#include "zmq.hpp"

#ifdef _DEBUG
#include <iostream>
#include <fstream>
#endif
#include <sstream>

#include <cstring>
#include <stdlib.h>
#include "Vbs3GetPos.pb.h"

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
sem_t sem;
pthread_t thread;
bool running;

void *listener_thread_func(void* arg)
{
	zmq::context_t listener_context(1);
	zmq::socket_t listener(listener_context, ZMQ_REP);
	listener.bind("tcp://*:5555");

#if _DEBUG
	ofstream file;
	file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
	file << "thread 2 start" << endl;
	file.close();
#endif

	while(running) {
		try {
#if _DEBUG
			file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
			file << "wait..." << endl;
			file.close();
#endif
			zmq::message_t request;

			//  Wait for next request from client
			listener.recv (&request);

			VBS3::Command command;
			command.ParseFromArray(request.data(), request.size());

			char *pos = new char[255];
			ExecuteCommand(command.cmd().c_str(), pos, 255);
			VBS3::Command cmdBuffer;
			cmdBuffer.set_cmd(pos);

			int size = cmdBuffer.ByteSize();
			void *data = malloc(size);
			cmdBuffer.SerializeToArray(data, size);
			listener.send(data, size);

			delete[] pos;
			free(data);

#if _DEBUG
//			file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
//			file << reply.data() << endl;
//			file.close();
#endif
		} catch(std::exception err) {
			running = false;
#if _DEBUG
			file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
			file << "thread 2 ex: " << err.what() << endl;
			file.close();
#endif
		}
	}

#if _DEBUG
	file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
	file << "thread 2 complete" << endl;
	file.close();
#endif

	return NULL;
}

int send_hello()
{
    //  Prepare our context and socket
    zmq::context_t context (1);
    zmq::socket_t socket (context, ZMQ_REQ);

    socket.connect ("tcp://localhost:5555");

    //  Do 10 requests, waiting each time for a response
    zmq::message_t request (5);
    memcpy (request.data (), "Hello", 5);
    socket.send (request);

    //  Get the reply.
    zmq::message_t reply;
    socket.recv (&reply);
    return 0;
}

void *thread_func(void* arg)
{
	running = true;
	//pthread_create(&listener_thread, NULL, winsock_thread, NULL);
	pthread_t listener_thread;
	pthread_create(&listener_thread, NULL, listener_thread_func, NULL);

	context = new zmq::context_t(1);
	publisher = new zmq::socket_t(*context, ZMQ_PUB);
	publisher->bind("tcp://*:5551");

//	while(running) {
//		Sleep(1000);
//		send_hello();
//	}
	sem_wait(&sem);

#if _DEBUG
	ofstream file;
	file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
	file << "message sending" << endl;
	file.close();
#endif

	running = false;
	//send_hello();

#if _DEBUG
	file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
	file << "message sent" << endl;
	file.close();
#endif

	//CANNOT CURRENTLY CLOSE W/O CRASH!
	delete publisher;
	//delete context;	// <-- causes crash if implemented
#if _DEBUG
	file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
	file << "Publisher Stopped." << endl;
	file.close();
#endif

	pthread_join(listener_thread, NULL);

#if _DEBUG
	file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
	file << "thread joined" << endl;
	file.close();
#endif

	return NULL;
}

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
	char* x = strtok(pos, "[],");
	char* y = strtok(NULL, "[],");
	posBuffer.set_x(atof(x));
	posBuffer.set_y(atof(y));
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

	ostringstream xbuffer;
	xbuffer << posBuffer.x();
	string xstr = xbuffer.str();

	ostringstream ybuffer;
	ybuffer << posBuffer.y();
	string ystr = ybuffer.str();

	string postocoord = "posToCoord [[" + xbuffer.str() + "," + ybuffer.str() + "], 'LL']";
	ExecuteCommand(postocoord.c_str(), pos, 255);
	posBuffer.set_lat(strtok(pos, "[],"));
	posBuffer.set_lon(strtok(NULL, "[],"));
#ifdef _DEBUG
	ofstream file;
	file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
	file << "posToCoord: " << postocoord << endl;
	file.close();
#endif

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

#ifdef _DEBUG
//		ofstream file;
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
#ifdef _DEBUG
	ofstream file;
	file.open("C:\\Users\\matta\\Desktop\\getPos.log", ofstream::out | ofstream::app);
#endif
	switch(fdwReason)
	{
		case DLL_PROCESS_ATTACH:
#ifdef _DEBUG
			file << "Called DllMain with DLL_PROCESS_ATTACH" << endl;
			file.flush();
#endif
			sem_init(&sem, 0, 0);
			Sleep(1000);
			pthread_create(&thread, NULL, thread_func, NULL);
			//OutputDebugString("Called DllMain with DLL_PROCESS_ATTACH\n");
		break;
		case DLL_PROCESS_DETACH:
#ifdef _DEBUG
			file << "Called DllMain with DLL_PROCESS_DETACH" << endl;
			file.flush();
#endif
			running = false;
			sem_post(&sem);
			Sleep(1000);
			sem_destroy(&sem);

			pthread_join(thread, NULL);

			//TODO: be able to shut down the listener objects to kill the listener thread.

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
#ifdef _DEBUG
	file.close();
#endif
	return TRUE;
}
