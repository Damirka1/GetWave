#ifndef CLIENT_HEADER
#define CLIENT_HEADER

#include <sys/socket.h>
#include <arpa/inet.h>
#include <poll.h>
#include "Vector.h"

struct Connection
{
	int socket;
	struct pollfd poll_in;
	struct sockaddr_in s_addr;
	char* ip;
	short port;

	int buffer_size;
	int command_buffer_size;
	char* message;
	char* result;
};


int CheckConnection(struct Connection* cnt);

struct Connection* Create();

int Connect(struct Connection* cnt, char* ip, const char* port);

int Send(struct Connection* cnt, int size);

int Recieve(struct Connection* cnt, int size);

void* Dispatch(struct Connection* cnt);

struct Vector* LoadBuffer(struct Connection* cnt, char* fsize);

void ReleaseConnection(struct Connection* cnt);

#endif