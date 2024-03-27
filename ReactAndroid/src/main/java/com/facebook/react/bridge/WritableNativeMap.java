/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.bridge;

import dalvik.annotation.optimization.FastNative;

import com.facebook.jni.HybridData;
import com.facebook.infer.annotation.Assertions;
import com.facebook.proguard.annotations.DoNotStrip;

/**
 * Implementation of a write-only map stored in native memory. Use
 * {@link Arguments#createMap()} if you need to stub out creating this class in a test.
 * TODO(5815532): Check if consumed on read
 */
@DoNotStrip
public class WritableNativeMap extends ReadableNativeMap implements WritableMap {
  static {
    ReactBridge.staticInit();
  }

  @FastNative
  @Override
  public native void putBoolean(String key, boolean value);
  @FastNative
  @Override
  public native void putDouble(String key, double value);
  @FastNative
  @Override
  public native void putInt(String key, int value);
  @FastNative
  @Override
  public native void putString(String key, String value);
  @FastNative
  @Override
  public native void putNull(String key);

  // Note: this consumes the map so do not reuse it.
  @Override
  public void putMap(String key, ReadableMap value) {
    Assertions.assertCondition(
        value == null || value instanceof ReadableNativeMap, "Illegal type provided");
    putNativeMap(key, (ReadableNativeMap) value);
  }

  // Note: this consumes the map so do not reuse it.
  @Override
  public void putArray(String key, ReadableArray value) {
    Assertions.assertCondition(
        value == null || value instanceof ReadableNativeArray, "Illegal type provided");
    putNativeArray(key, (ReadableNativeArray) value);
  }

  // Note: this **DOES NOT** consume the source map
  @Override
  public void merge(ReadableMap source) {
    Assertions.assertCondition(source instanceof ReadableNativeMap, "Illegal type provided");
    mergeNativeMap((ReadableNativeMap) source);
  }

  public WritableNativeMap() {
    super(initHybrid());
  }

  @FastNative
  private static native HybridData initHybrid();

  @FastNative
  private native void putNativeMap(String key, ReadableNativeMap value);
  @FastNative
  private native void putNativeArray(String key, ReadableNativeArray value);
  @FastNative
  private native void mergeNativeMap(ReadableNativeMap source);
}
