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
	char* port;

	int buffer_size;
	int command_buffer_size;
	char* message;
	char* result;
};


void CheckAndReconnect(struct Connection* cnt);

struct Connection* Create();

int Connect(struct Connection* cnt, char* ip, char* port);

long long Send(struct Connection* cnt, long long size);

long long Receieve(struct Connection* cnt, long long size);

void* Dispatch(struct Connection* cnt);

struct Vector* LoadBuffer(struct Connection* cnt, char* fsize);

void ReleaseConnection(struct Connection* cnt);

#endif
