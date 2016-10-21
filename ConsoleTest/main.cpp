#include <windows.h>
#include <stdlib.h>
#include <string.h>
#include <tchar.h>
#include <pthread.h>
#include <semaphore.h>
#include "zmq.hpp"

#include <iostream>

zmq::socket_t *publisher;
zmq::context_t *context;
sem_t sem;
pthread_t thread;
bool running;

void *listener_thread_func(void* arg)
{
	zmq::context_t listener_context(1);
	zmq::socket_t listener(listener_context, ZMQ_REP);
	listener.bind("tcp://*:5552");

	while(running) {
		try {
			zmq::message_t request;

			//  Wait for next request from client
			listener.recv (&request);
			std::cout << request.data() << std::endl;
		} catch(std::exception err) {
			running = false;
			std::cerr << "Err: " << err.what() << std::endl;
		}

        ////  Send reply back to client
        //zmq::message_t reply (5);
        //memcpy (reply.data (), "World", 5);
        //socket.send (reply);
	}

	std::cout << "thread 2 complete" << std::endl;

	return NULL;
}

void *thread_func(void* arg)
{
	//initialize the server
	pthread_t listener_thread;
	pthread_create(&listener_thread, NULL, listener_thread_func, NULL);

	context = new zmq::context_t(1);
	publisher = new zmq::socket_t(*context, ZMQ_PUB);
	publisher->bind("tcp://*:5551");

	sem_wait(&sem);

	//kill the server...
	running = false;
	zmq::socket_t sock(*context, ZMQ_REQ);
	zmq::message_t request(5);
	memcpy (request.data (), "Hello", 5);
	sock.connect("tcp://localhost:5552");
	sock.send(request);

	pthread_join(listener_thread, NULL);

	//CANNOT CURRENTLY CLOSE W/O CRASH!
	delete publisher;
//	delete context;	// <-- causes crash if implemented
	std::cout << "thread 1 complete" << std::endl;
	return NULL;
}

int main(int argc, char** argv) {
	sem_init(&sem, 0, 0);
	Sleep(1000);
	running = true;
	pthread_create(&thread, NULL, thread_func, NULL);
	Sleep(1000);

	sem_post(&sem);
	Sleep(1000);
	sem_destroy(&sem);

	pthread_join(thread, NULL);
	return 0;
}