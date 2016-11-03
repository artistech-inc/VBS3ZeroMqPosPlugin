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

#define WIN32_LEAN_AND_MEAN

#include <winsock2.h>
#include <ws2tcpip.h>


// Need to link with Ws2_32.lib, Mswsock.lib, and Advapi32.lib
#pragma comment (lib, "Ws2_32.lib")


#define DEFAULT_BUFLEN 512
#define DEFAULT_PORT "5555"

using namespace std;

void* winsock_thread(void* arg) 
{
    WSADATA wsaData;
    int iResult;

    SOCKET ListenSocket = INVALID_SOCKET;
    SOCKET ClientSocket = INVALID_SOCKET;

    struct addrinfo *result = NULL;
    struct addrinfo hints;

    int iSendResult;
    char recvbuf[DEFAULT_BUFLEN];
    int recvbuflen = DEFAULT_BUFLEN;
    
    // Initialize Winsock
    iResult = WSAStartup(MAKEWORD(2,2), &wsaData);
    if (iResult != 0) {
        printf("WSAStartup failed with error: %d\n", iResult);
        return NULL;
    }

    ZeroMemory(&hints, sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = IPPROTO_TCP;
    hints.ai_flags = AI_PASSIVE;

    // Resolve the server address and port
    iResult = getaddrinfo(NULL, DEFAULT_PORT, &hints, &result);
    if ( iResult != 0 ) {
        printf("getaddrinfo failed with error: %d\n", iResult);
        WSACleanup();
        return NULL;
    }

    // Create a SOCKET for connecting to server
    ListenSocket = socket(result->ai_family, result->ai_socktype, result->ai_protocol);
    if (ListenSocket == INVALID_SOCKET) {
        printf("socket failed with error: %ld\n", WSAGetLastError());
        freeaddrinfo(result);
        WSACleanup();
        return NULL;
    }

    // Setup the TCP listening socket
    iResult = bind( ListenSocket, result->ai_addr, (int)result->ai_addrlen);
    if (iResult == SOCKET_ERROR) {
        printf("bind failed with error: %d\n", WSAGetLastError());
        freeaddrinfo(result);
        closesocket(ListenSocket);
        WSACleanup();
        return NULL;
    }

    freeaddrinfo(result);

    iResult = listen(ListenSocket, SOMAXCONN);
    if (iResult == SOCKET_ERROR) {
        printf("listen failed with error: %d\n", WSAGetLastError());
        closesocket(ListenSocket);
        WSACleanup();
        return NULL;
    }

    // Accept a client socket
    ClientSocket = accept(ListenSocket, NULL, NULL);
    if (ClientSocket == INVALID_SOCKET) {
        printf("accept failed with error: %d\n", WSAGetLastError());
        closesocket(ListenSocket);
        WSACleanup();
        return NULL;
    }

    // No longer need server socket
    closesocket(ListenSocket);

    // Receive until the peer shuts down the connection
    do {

        iResult = recv(ClientSocket, recvbuf, recvbuflen, 0);
        if (iResult > 0) {
            printf("Bytes received: %d\n", iResult);

        // Echo the buffer back to the sender
            iSendResult = send( ClientSocket, recvbuf, iResult, 0 );
            if (iSendResult == SOCKET_ERROR) {
                printf("send failed with error: %d\n", WSAGetLastError());
                closesocket(ClientSocket);
                WSACleanup();
                return NULL;
            }
            printf("Bytes sent: %d\n", iSendResult);
        }
        else if (iResult == 0)
            printf("Connection closing...\n");
        else  {
            printf("recv failed with error: %d\n", WSAGetLastError());
            closesocket(ClientSocket);
            WSACleanup();
            return NULL;
        }

    } while (iResult > 0);

    // shutdown the connection since we're done
    iResult = shutdown(ClientSocket, SD_SEND);
    if (iResult == SOCKET_ERROR) {
        printf("shutdown failed with error: %d\n", WSAGetLastError());
        closesocket(ClientSocket);
        WSACleanup();
        return NULL;
    }

    // cleanup
    closesocket(ClientSocket);
    WSACleanup();

    return NULL;
}

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
	pthread_create(&listener_thread, NULL, winsock_thread, NULL);

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