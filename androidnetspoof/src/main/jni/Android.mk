LOCAL_PATH := $(call my-dir)
MY_LOCAL_PATH := $(LOCAL_PATH)
include $(CLEAR_VARS)

include $(MY_LOCAL_PATH)/libpcap/Android.mk
include $(MY_LOCAL_PATH)/libnet/Android.mk
include $(MY_LOCAL_PATH)/arpspoof/Android.mk
include $(MY_LOCAL_PATH)/arp-scan/Android.mk

LOCAL_PATH := $(MY_LOCAL_PATH)
include $(CLEAR_VARS)

LOCAL_MODULE    := netspooflib
LOCAL_SRC_FILES := uk_digitalsquid_netspoofer_JNI.c

include $(BUILD_SHARED_LIBRARY)
