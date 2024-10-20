// Copyright (c) 2004-present, Facebook, Inc.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

#pragma once

#include <fbjni/fbjni.h>
#include <folly/dynamic.h>

#include "NativeCommon.h"

namespace facebook {
namespace react {

class NativeArray : public jni::HybridClass<NativeArray> {
 public:
  static constexpr const char* kJavaDescriptor = "Lcom/facebook/react/bridge/NativeArray;";

  jni::local_ref<jstring> toString();

  RN_EXPORT folly::dynamic consume();

  // Whether this array has been added to another array or map and no longer
  // has a valid array value.
  bool isConsumed;
  void throwIfConsumed();

  static void registerNatives();

 protected:
  folly::dynamic array_;

  friend HybridBase;
  explicit NativeArray(folly::dynamic array);
};

}}
