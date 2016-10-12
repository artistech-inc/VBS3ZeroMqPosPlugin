#include <windows.h>
#include <stdlib.h>
#include <string.h>
#include <tchar.h>
#include <pthread.h>
#include <semaphore.h>
#include "zmq.hpp"

#include <iostream>

zmq::socket_t *publisher;
zmq::socket_t *listener;
zmq::context_t *context;
zmq::context_t *listener_context;
sem_t sem;
pthread_t thread;
pthread_t listener_thread;
bool running;

void *thread_func(void* arg)
{
	//zsock_t *pub = zsock_new_pub ("tcp://*:5552");
	context = new zmq::context_t(1);
	publisher = new zmq::socket_t(*context, ZMQ_PUB);
	publisher->bind("tcp://*:5551");

	//zctx_t *ctx = zctx_new();
 //   void *pub = zsocket_new(ctx, ZMQ_SUB);
	//zsocket_bind(pub, "tcp://*:5552");

	sem_wait(&sem);

	//CANNOT CURRENTLY CLOSE W/O CRASH!
	delete publisher;
	//delete context;	// <-- causes crash if implemented

	return NULL;
}

void *listener_thread_func(void* arg)
{
	listener_context = new zmq::context_t(1);
	listener = new zmq::socket_t(*listener_context, ZMQ_REP);
	listener->bind("tcp://*:5552");

	while(running) {
        zmq::message_t request;

        //  Wait for next request from client
        listener->recv (&request);
        //std::cout << "Received Hello" << std::endl;

        ////  Send reply back to client
        //zmq::message_t reply (5);
        //memcpy (reply.data (), "World", 5);
        //socket.send (reply);
	}

	return NULL;
}

int main(int argc, char** argv) {
	std::cout<<"hello"<<std::endl;
	return 0;
}