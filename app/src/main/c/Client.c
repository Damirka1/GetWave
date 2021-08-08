#include "Client.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <jni.h>
#include <netdb.h>
#include "Author.h"
#include "Track.h"
#include "android/log.h"

struct Connection* Create()
{
	struct Connection* cnt = malloc(sizeof(struct Connection));
	cnt->buffer_size = 131072; // 128 kb.
	cnt->command_buffer_size = 2048; // 2kb.
	cnt->message = malloc(cnt->buffer_size);
	cnt->result = malloc(cnt->buffer_size);
    memset(cnt->message, 0, cnt->buffer_size);
    memset(cnt->result, 0, cnt->buffer_size);
	cnt->ip = 0;
	return cnt;
}

int Connect(struct Connection* cnt, char* ip, const char* port)
{
    struct addrinfo hints, *servinfo;

    memset(&hints, 0, sizeof(hints));

    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;

    if(getaddrinfo(ip, port, &hints, &servinfo) != 0)
    {
        return -1;
    }

	if ((cnt->socket = socket(servinfo->ai_family, servinfo->ai_socktype, servinfo->ai_protocol)) == -1)
	{
		return -1;
	}

	if (connect(cnt->socket, servinfo->ai_addr, servinfo->ai_addrlen) == -1)
	{
		return -1;
	}

	//fcntl(cnt->socket, F_SETFL, O_NONBLOCK);

    cnt->poll_in.fd = cnt->socket;
	cnt->poll_in.events = POLLIN;

	return 0;
}


void ClearBuffer(void* buffer, int buffer_size)
{
    memset(buffer, 0, buffer_size);
}


int Send(struct Connection* cnt, int size)
{
    int count = 0;
    int attemps = 0;
    while(count < size)
    {
        int r = send(cnt->socket, cnt->message + count, size - count, 0);
        if(r <= 0) {
            if (attemps++ == 5)
                return -1;
            //usleep(100);
            continue;
        }
        count += r;
    }
    ClearBuffer(cnt->message, cnt->buffer_size);
    return count;
}

int Recieve(struct Connection* cnt, int size)
{
    ClearBuffer(cnt->result, cnt->buffer_size);

    int count = 0;
    int attemps = 0;

    while(count < size)
    {
        int r = recv(cnt->socket, cnt->result + count, size - count, MSG_WAITALL);
        if(r <= 0)
        {
            if(attemps++ == 5)
                return -1;
            usleep(100);
            continue;
        }
        count += r;
    }
    return count;
}

struct Vector* LoadBuffer(struct Connection* cnt, char* fsize)
{
	long long Filesize = 0;
	memcpy(&Filesize, fsize + 1, sizeof(long long));

	struct Vector* vec = CreateVector(Filesize);
	vec->pData = malloc(Filesize);

	long long it = 0;
	long long ReadSize = Filesize;

	if(Filesize > cnt->buffer_size)
	    ReadSize = cnt->buffer_size;
	while(it < Filesize)
	{
	    Recieve(cnt, ReadSize);
        if(it + ReadSize > Filesize)
        {
            ReadSize = Filesize - it;
            if(ReadSize < 0)
                ReadSize = Filesize;
        }
        memcpy(vec->pData + it, cnt->result, ReadSize);
        it += ReadSize;
	}
	vec->Size = 1;
	return vec;
}

void* Dispatch(struct Connection* cnt)
{
    ClearBuffer(cnt->message, cnt->buffer_size);
	if(Recieve(cnt, cnt->command_buffer_size) <= 0)
	    return 0;

    char* saveptr;
    char* token = strtok_r(cnt->result, " ", &saveptr);

    if(strcmp(token, "file") == 0)
    {
        char* fsize = cnt->result + strlen(token);
        struct Vector* vec = LoadBuffer(cnt, fsize);
        return vec;
    }
    else if (strcmp(token, "filesize") == 0)
    {
        char* fsize = cnt->result + strlen(token);

        long long Filesize = 0;
        memcpy(&Filesize, fsize + 1, sizeof(long long));

        struct Vector* vec = CreateVector(Filesize);
        return vec;
    }
    else if (strcmp(token, "musics") == 0)
    {
        char* fsize = cnt->result + strlen(token);
        struct Vector* vec = LoadBuffer(cnt, fsize);
        return vec;
        //Test(vec->pData, vec->DataSize);
    }
    else if (strcmp(token, "tracks") == 0)
    {
        char* fsize = cnt->result + strlen(token);
        struct Vector* vec = LoadBuffer(cnt, fsize);
        vec->Size = vec->DataSize / sizeof(struct Track);
        vec->DataSize = sizeof(struct Track);
        return vec;
    }
    else if (strcmp(token, "streamdata") == 0)
    {
        char* fsize = cnt->result + strlen(token);
        struct Vector* vec = LoadBuffer(cnt, fsize);
        return vec;
    }

    return 0;
}


void ReleaseConnection(struct Connection* cnt)
{
    //free(cnt->ip);
	free(cnt->message);
	free(cnt->result);
	free(cnt);
}
