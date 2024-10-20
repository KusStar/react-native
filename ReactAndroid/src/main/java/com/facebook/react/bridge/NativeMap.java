/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.bridge;

import dalvik.annotation.optimization.FastNative;

import com.facebook.jni.HybridData;
import com.facebook.proguard.annotations.DoNotStrip;

/**
 * Base class for a Map whose keys and values are stored in native code (C++).
 */
@DoNotStrip
public abstract class NativeMap {
  static {
    ReactBridge.staticInit();
  }

  public NativeMap(HybridData hybridData) {
    mHybridData = hybridData;
  }

  @FastNative
  @Override
  public native String toString();

  @DoNotStrip
  private HybridData mHybridData;
}
