LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := opsqlite

LOCAL_SRC_FILES := $(wildcard \
$(LOCAL_PATH)/opsqlite/*.cpp \
$(LOCAL_PATH)/opsqlite/*.c \
)

LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

LOCAL_LDLIBS := -llog
LOCAL_CFLAGS := -fexceptions -frtti -O3

LOCAL_STATIC_LIBRARIES := callinvoker
LOCAL_SHARED_LIBRARIES := libjsi

include $(BUILD_SHARED_LIBRARY)
