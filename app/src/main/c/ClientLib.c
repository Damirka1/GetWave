#include <alloca.h>
#include <jni.h>
#include "Client.h"
#include "Track.h"
#include <stdlib.h>
#include <string.h>

#include <android/log.h>

static struct Connection* cnt;
//static char* ip = "31.169.0.169";
static char* ip = "192.168.1.30";
static char* port = "25565";
static struct Vector* Tracks = NULL;

extern JNIEXPORT jboolean
Java_su_damirka_getwave_connection_ConnectionService_Connect(JNIEnv* env, jobject this)
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
    CheckAndReconnect(cnt);

    strcpy(cnt->message, "gettracks");

    Send(cnt, cnt->command_buffer_size);

    if(Tracks != NULL)
        Release(Tracks);

    Tracks = Dispatch(cnt);

    jclass trackClass = (*env)->FindClass(env, "su/damirka/getwave/music/Track");
    jfieldID IdID = (*env)->GetFieldID(env, trackClass, "Id", "J");
    jfieldID PathID = (*env)->GetFieldID(env, trackClass, "Path", "Ljava/lang/String;");
    jfieldID TitleID = (*env)->GetFieldID(env, trackClass, "Title", "Ljava/lang/String;");
    jfieldID AuthorID = (*env)->GetFieldID(env, trackClass, "Author", "Ljava/lang/String;");
    jfieldID ArtPathID = (*env)->GetFieldID(env, trackClass, "ArtPath", "Ljava/lang/String;");

    if(Tracks == 0)
    {
        jobjectArray JavaTracks = (*env)->NewObjectArray(env, 1, trackClass, NULL);

        jmethodID constructor = (*env)->GetMethodID(env, trackClass, "<init>", "()V");
        jobject instance = (*env)->NewObject(env, trackClass, constructor);

        (*env)->SetLongField(env, instance, IdID, -1LL);
        (*env)->SetObjectField(env, instance, PathID, (*env)->NewStringUTF(env, "-1"));
        (*env)->SetObjectField(env, instance, TitleID, (*env)->NewStringUTF(env, "-1"));
        (*env)->SetObjectField(env, instance, AuthorID, (*env)->NewStringUTF(env, "-1"));
        (*env)->SetObjectField(env, instance, ArtPathID, (*env)->NewStringUTF(env, "-1"));

        (*env)->SetObjectArrayElement(env, JavaTracks, 0, instance);

        return JavaTracks;
    }

    jobjectArray JavaTracks = (*env)->NewObjectArray(env, Tracks->Size, trackClass, NULL);

    for(int i = 0; i < Tracks->Size; i++)
    {
        struct Track* track = Get(Tracks, i);

        jmethodID constructor = (*env)->GetMethodID(env, trackClass, "<init>", "()V");
        jobject instance = (*env)->NewObject(env, trackClass, constructor);

        (*env)->SetLongField(env, instance, IdID, track->Id);
        (*env)->SetObjectField(env, instance, PathID, (*env)->NewStringUTF(env, track->Path));
        (*env)->SetObjectField(env, instance, TitleID, (*env)->NewStringUTF(env, track->Title));
        (*env)->SetObjectField(env, instance, AuthorID, (*env)->NewStringUTF(env, track->Author));
        (*env)->SetObjectField(env, instance, ArtPathID, (*env)->NewStringUTF(env, track->ArtPath));

        (*env)->SetObjectArrayElement(env, JavaTracks, i, instance);
    }

    return JavaTracks;
}

extern JNIEXPORT jbyteArray
Java_su_damirka_getwave_connection_ConnectionService_GetFileFromServer(JNIEnv* env, jobject this, jstring Path)
{
    struct Connection* filecnt = Create();

    if(Connect(filecnt, ip, port) == 0)
    {
        const char* nativePath = (*env)->GetStringUTFChars(env, Path, 0);
        strcpy(filecnt->message, "getfile ");

        memcpy(filecnt->message + strlen(filecnt->message), nativePath, strlen(nativePath));
        Send(filecnt, filecnt->command_buffer_size);
        (*env)->ReleaseStringUTFChars(env, Path, nativePath);

        struct Vector* File = Dispatch(filecnt);

        if(File == 0)
        {
            jbyteArray array = (*env)->NewByteArray(env, 1);
            char res = (char)-1;
            (*env)->SetByteArrayRegion(env, array, 0, 1, (jbyte *) &res);
            return array;
        }

        jbyteArray array = (*env)->NewByteArray(env, File->DataSize);
        (*env)->SetByteArrayRegion(env, array, 0, File->DataSize, File->pData);

        Release(File);
        ReleaseConnection(filecnt);

        return array;
    }
    else
    {
        jbyteArray array = (*env)->NewByteArray(env, 1);
        char res = (char)-1;
        (*env)->SetByteArrayRegion(env, array, 0, 1, (jbyte *) &res);
        return array;
    }
}

extern JNIEXPORT jlong
Java_su_damirka_getwave_music_StreamMediaDataSource_GetSizeofFile(JNIEnv* env, jobject this, jstring Path)
{
    CheckAndReconnect(cnt);

    const char* nativePath = (*env)->GetStringUTFChars(env, Path, 0);
    strcpy(cnt->message, "getsizeof ");

    memcpy(cnt->message + strlen(cnt->message), nativePath, strlen(nativePath));

    Send(cnt, cnt->command_buffer_size);

    struct Vector* vec = Dispatch(cnt);

    if(vec == 0)
        return -1;

    return vec->DataSize;
}

extern jint
Java_su_damirka_getwave_music_StreamMediaDataSource_GetStreamFromServer(JNIEnv* env, jobject this, jstring Path, jbyteArray buffer, jint offset, jlong position, jint size)
{
    CheckAndReconnect(cnt);

    char pos[12];
    char s[12];
    sprintf(pos, ";%lld;", (long long)position);
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