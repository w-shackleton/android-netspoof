LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include $(LOCAL_PATH)/arpspoof/Android.mk
include $(LOCAL_PATH)/libpcap/Android.mk
include $(LOCAL_PATH)/libnet/Android.mk

LOCAL_MODULE    := netspooflib
LOCAL_SRC_FILES := uk_digitalsquid_netspoofer_JNI.c

include $(BUILD_SHARED_LIBRARY)
