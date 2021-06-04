#include <jni.h>
#include <string>
#include <filesystem>
#include <vector>

std::string jstring2string(JNIEnv *env, jstring jStr) {
    if (!jStr)
        return "";

    const jclass stringClass = env->GetObjectClass(jStr);
    const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

    size_t length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte* pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    std::string ret = std::string((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_su_damirka_getwave_WindowManager_GetAllMusic(JNIEnv* env, jobject /* this */, jstring Path)
{
    std::filesystem::directory_iterator it(jstring2string(env, Path));

    std::vector<std::filesystem::directory_entry> Files;

    for(const auto& file : it)
    {
        if(std::filesystem::path(file.path()).extension() == ".mp3")
            Files.push_back(file);
    }

    std::sort(Files.begin(), Files.end(), [&](std::filesystem::directory_entry& a, std::filesystem::directory_entry& b)
    {
        return a.last_write_time() > b.last_write_time();
    });

    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray FilesPath = env->NewObjectArray(Files.size(), stringClass, NULL);

    for(int i = 0; i < Files.size(); i++)
        env->SetObjectArrayElement(FilesPath, i, env->NewStringUTF(Files[i].path().c_str()));

    return FilesPath;
}