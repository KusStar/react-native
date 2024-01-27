LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := quickjs

LOCAL_SRC_FILES := $(wildcard \
$(LOCAL_PATH)/quickjs/*.cpp \
$(LOCAL_PATH)/quickjs/engine/*.c \
)

LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_C_INCLUDES)

QJS_FLAGS := -DCONFIG_VERSION="1" \
        -D_GNU_SOURCE \
        -Wno-unused-variable \
        -DCONFIG_CC="gcc" \
        -DLOG_TAG=\"ReactNative\" \
        -DENABLE_HASH_CHECK=1 \
        -DCONFIG_BIGNUM=0 \

LOCAL_CFLAGS := -fexceptions -frtti -O3 ${QJS_FLAGS} -Wno-unused-but-set-variable -Wno-pointer-sign  -Wno-return-type

LOCAL_LDLIBS := -llog -ldl -landroid
LOCAL_EXPORT_LDLIBS := -llog

LOCAL_SHARED_LIBRARIES := libjsi libfolly_json glog

include $(BUILD_SHARED_LIBRARY)
