LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := arp-scan

LOCAL_SRC_FILES := arp-scan/arp-scan.c \
  arp-scan/error.c \
  arp-scan/wrappers.c \
  arp-scan/utils.c \
  arp-scan/hash.c \
  arp-scan/obstack.c \
  arp-scan/mt19937ar.c \
	arp-scan/link-packet-socket.c

APP_OPTIM := release

LOCAL_CFLAGS:= -static -ffunction-sections -fdata-sections -DHAVE_CONFIG_H
LOCAL_LDFLAGS += -Wl,--gc-sections

LOCAL_C_INCLUDES := $(LOCAL_PATH) $(LOCAL_PATH)/../libpcap $(LOCAL_PATH)/arp-scan
LOCAL_STATIC_LIBRARIES := libpcap

include $(BUILD_EXECUTABLE)
