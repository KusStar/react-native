# Copyright (c) Facebook, Inc. and its affiliates.
#
# This source code is licensed under the MIT license found in the
# LICENSE file in the root directory of this source tree.

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := hermes-executor-release

LOCAL_CFLAGS := -fexceptions -frtti -O3

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/*.cpp)

LOCAL_CPP_FEATURES := exceptions

LOCAL_STATIC_LIBRARIES := libjsireact libjsi libhermes-executor-common-release
LOCAL_SHARED_LIBRARIES := libfolly_json libfb libfbjni libreactnativejni libhermes

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := hermes-executor-debug
LOCAL_CFLAGS := -DHERMES_ENABLE_DEBUGGER=0

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/*.cpp)

LOCAL_CPP_FEATURES := exceptions

LOCAL_STATIC_LIBRARIES := libjsireact libjsi libhermes-executor-common-debug
LOCAL_SHARED_LIBRARIES := libfolly_json libfb libfbjni libreactnativejni libhermes

include $(BUILD_SHARED_LIBRARY)
