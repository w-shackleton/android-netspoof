LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := netspooflib
LOCAL_SRC_FILES := uk_digitalsquid_netspoofer_JNI.c

include $(BUILD_SHARED_LIBRARY)
