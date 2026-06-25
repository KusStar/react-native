APP_BUILD_SCRIPT := Android.mk

APP_ABI := armeabi-v7a x86 arm64-v8a x86_64
APP_PLATFORM := android-21

APP_MK_DIR := $(dir $(lastword $(MAKEFILE_LIST)))

# What is NDK_MODULE_PATH?
#   This is comparable to the PATH environment variable in Linux. The purpose
#   of NDK_MODULE_PATH is to provide a list of directories that contain modules
#   we want ndk-build to compile.
#
# What is HOST_DIRSEP?
#   In PATH, the directories are separated by a ':'.
#   In NDK_MODULE_PATH, the directories are separated by $(HOST_DIRSEP).
#
# Where are APP_MK_DIR, THIRD_PARTY_NDK_DIR, etc. defined?
#   The directories inside NDK_MODULE_PATH (ex: APP_MK_DIR, THIRD_PARTY_NDK_DIR,
#   etc.) are defined inside build.gradle.
NDK_MODULE_PATH := $(APP_MK_DIR)$(HOST_DIRSEP)$(THIRD_PARTY_NDK_DIR)$(HOST_DIRSEP)$(REACT_COMMON_DIR)$(HOST_DIRSEP)$(APP_MK_DIR)first-party$(HOST_DIRSEP)$(REACT_SRC_DIR)

APP_STL := c++_shared

# Make sure every shared lib includes a .note.gnu.build-id header
APP_CFLAGS := -Wall -Werror
APP_CPPFLAGS := -std=c++17
APP_LDFLAGS := -Wl,--build-id
APP_LDFLAGS += -Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384

# 16KB page-size support for NDK r27+. This also hides the PAGE_SIZE macro so
# fixed page-size assumptions fail at compile time.
APP_SUPPORT_FLEXIBLE_PAGE_SIZES := true

# ndk27 compile error
APP_CPPFLAGS += -Wno-vla-cxx-extension -Wno-deprecated-builtins -Wno-deprecated-declarations
APP_CPPFLAGS += -D_LIBCPP_DISABLE_DEPRECATION_WARNINGS -D_LIBCPP_ENABLE_CXX17_REMOVED_UNARY_BINARY_FUNCTION

NDK_TOOLCHAIN_VERSION := clang
