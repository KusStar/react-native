LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
  folly/json.cpp \
  folly/Unicode.cpp \
  folly/Conv.cpp \
  folly/Demangle.cpp \
  folly/memory/detail/MallocImpl.cpp \
  folly/String.cpp \
  folly/dynamic.cpp \
  folly/FileUtil.cpp \
  folly/Format.cpp \
  folly/net/NetOps.cpp \
  folly/json_pointer.cpp \
  folly/lang/CString.cpp \
  folly/lang/SafeAssert.cpp \
  folly/detail/UniqueInstance.cpp \
  folly/hash/SpookyHashV2.cpp \
  folly/container/detail/F14Table.cpp \
  folly/ScopeGuard.cpp \
  folly/portability/SysUio.cpp \
  folly/lang/ToAscii.cpp

ifeq ($(APP_OPTIM),debug)
  LOCAL_SRC_FILES += \
    folly/lang/Assume.cpp
endif

LOCAL_C_INCLUDES := $(LOCAL_PATH)
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)

LOCAL_CFLAGS += -fexceptions -fno-omit-frame-pointer -frtti -Wno-sign-compare

FOLLY_FLAGS := \
  -DFOLLY_NO_CONFIG=1 \
  -DFOLLY_HAVE_CLOCK_GETTIME=1 \
  -DFOLLY_USE_LIBCPP=1 \
  -DFOLLY_MOBILE=1 \
  -DFOLLY_HAVE_RECVMMSG=1 \

# If APP_PLATFORM in Application.mk targets android-23 above, please comment this line.
# NDK uses GNU style stderror_r() after API 23.
FOLLY_FLAGS += -DFOLLY_HAVE_XSI_STRERROR_R=1

LOCAL_CFLAGS += $(FOLLY_FLAGS)

LOCAL_EXPORT_CPPFLAGS := $(FOLLY_FLAGS)

LOCAL_MODULE := libfolly_json

LOCAL_SHARED_LIBRARIES := libglog libdouble-conversion
# Boost is header-only library we pretend to link is statically as
# this way android makefile will automatically setup path to boost header
# file, but except from that this will have no effect, as no c/cpp files
# are part of this static library
LOCAL_STATIC_LIBRARIES := libboost

include $(BUILD_SHARED_LIBRARY)

$(call import-module,glog)
$(call import-module,double-conversion)
$(call import-module,boost)
