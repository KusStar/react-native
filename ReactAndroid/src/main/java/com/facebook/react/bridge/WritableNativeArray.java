/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.bridge;

import dalvik.annotation.optimization.FastNative;

import com.facebook.infer.annotation.Assertions;
import com.facebook.jni.HybridData;
import com.facebook.proguard.annotations.DoNotStrip;

/**
 * Implementation of a write-only array stored in native memory. Use
 * {@link Arguments#createArray()} if you need to stub out creating this class in a test.
 * TODO(5815532): Check if consumed on read
 */
@DoNotStrip
public class WritableNativeArray extends ReadableNativeArray implements WritableArray {
  static {
    ReactBridge.staticInit();
  }

  public WritableNativeArray() {
    super(initHybrid());
  }

  @FastNative
  @Override
  public native void pushNull();
  @FastNative
  @Override
  public native void pushBoolean(boolean value);
  @FastNative
  @Override
  public native void pushDouble(double value);
  @FastNative
  @Override
  public native void pushInt(int value);
  @FastNative
  @Override
  public native void pushString(String value);

  // Note: this consumes the map so do not reuse it.
  @Override
  public void pushArray(ReadableArray array) {
    Assertions.assertCondition(
        array == null || array instanceof ReadableNativeArray, "Illegal type provided");
    pushNativeArray((ReadableNativeArray) array);
  }

  // Note: this consumes the map so do not reuse it.
  @Override
  public void pushMap(ReadableMap map) {
    Assertions.assertCondition(
        map == null || map instanceof ReadableNativeMap, "Illegal type provided");
    pushNativeMap((ReadableNativeMap) map);
  }

  @FastNative
  private static native HybridData initHybrid();
  @FastNative
  private native void pushNativeArray(ReadableNativeArray array);
  @FastNative
  private native void pushNativeMap(ReadableNativeMap map);
}
