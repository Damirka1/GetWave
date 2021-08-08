
#include <jni.h>
#include "Client.h"
#include "Track.h"
#include <stdlib.h>
#include <string.h>

#include <android/log.h>

static struct Connection* cnt;
//static char* ip = "37.151.231.229";
static char* ip = "192.168.1.30";
static char* port = "25565";
static struct Vector* Tracks = NULL;

extern JNIEXPORT jboolean
Java_su_damirka_getwave_Application_Connect(JNIEnv* env, jobject this)
{
    cnt = Create();
    if(Connect(cnt, ip, port) == 0)
        return 1;
    else
        return 0;
}


extern JNIEXPORT jobjectArray
Java_su_damirka_getwave_connection_ConnectionService_LoadAllMusicFromServer(JNIEnv* env, jobject this)
{
    strcpy(cnt->message, "gettracks");

    Send(cnt, cnt->command_buffer_size);

    if(Tracks != NULL)
        Release(Tracks);

    Tracks = Dispatch(cnt);
    jclass stringClass = (*env)->FindClass(env, "java/lang/String");
    jobjectArray FilesPath = (*env)->NewObjectArray(env, Tracks->Size, stringClass, NULL);

    for(int i = 0; i < Tracks->Size; i++)
    {
        char* str = malloc(1024);
        memset(str, 0, 1024);
        memcpy(str, Tracks->pData + (i * Tracks->DataSize), 1024);

        (*env)->SetObjectArrayElement(env, FilesPath, i, (*env)->NewStringUTF(env, str));
        free(str);
    }

    return FilesPath;
}

extern JNIEXPORT jbyteArray
Java_su_damirka_getwave_music_StreamMediaDataSource_GetFileFromServer(JNIEnv* env, jobject this, jstring Path)
{
    const char* nativePath = (*env)->GetStringUTFChars(env, Path, 0);
    strcpy(cnt->message, "getfile ");

    memcpy(cnt->message + strlen(cnt->message), nativePath, strlen(nativePath));
    Send(cnt, cnt->command_buffer_size);
    (*env)->ReleaseStringUTFChars(env, Path, nativePath);

    struct Vector* File = Dispatch(cnt);
    jbyteArray array = (*env)->NewByteArray(env, File->DataSize);
    (*env)->SetByteArrayRegion(env, array, 0, File->DataSize, File->pData);

    Release(File);

    return array;
}

extern JNIEXPORT jlong
Java_su_damirka_getwave_music_StreamMediaDataSource_GetSizeofFile(JNIEnv* env, jobject this, jstring Path)
{
    const char* nativePath = (*env)->GetStringUTFChars(env, Path, 0);
    strcpy(cnt->message, "getsizeof ");

    memcpy(cnt->message + strlen(cnt->message), nativePath, strlen(nativePath));

    Send(cnt, cnt->command_buffer_size);

    struct Vector* vec = Dispatch(cnt);
    return vec->DataSize;
}

extern jint
Java_su_damirka_getwave_music_StreamMediaDataSource_GetStreamFromServer(JNIEnv* env, jobject this, jstring Path, jbyteArray buffer, jint offset, jlong position, jint size) {
    char pos[12];
    char s[12];
    sprintf(pos, ";%lld;", position);
    sprintf(s, "%d;", size);

    const char *nativePath = (*env)->GetStringUTFChars(env, Path, 0);

    strcpy(cnt->message, "getstream ");

    strcpy(cnt->message + strlen(cnt->message), nativePath);
    strcpy(cnt->message + strlen(cnt->message), pos);

    strcpy(cnt->message + strlen(cnt->message), s);

    Send(cnt, cnt->command_buffer_size);
    (*env)->ReleaseStringUTFChars(env, Path, nativePath);

    struct Vector *Stream = Dispatch(cnt);


    if (Stream != 0 && Stream->DataSize > 0) {
        (*env)->SetByteArrayRegion(env, buffer, offset, Stream->DataSize, Stream->pData);
        Release(Stream);
        return Stream->DataSize;
    } else {
        return -1;
    }

}

JNIEXPORT jint JNICALL
Java_su_damirka_getwave_music_MetaMediaDataSource_GetStreamFromServer(JNIEnv *env, jobject this,
                                                                      jstring path,
                                                                      jbyteArray buffer,
                                                                      jint offset, jlong position,
                                                                      jint size) {
    return Java_su_damirka_getwave_music_StreamMediaDataSource_GetStreamFromServer(env, this, path, buffer, offset, position, size);
}