LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := Client-Connection
LOCAL_SRC_FILES :=  Client.c ClientLib.c Vector.c

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)

