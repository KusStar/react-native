/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.bridge.queue;

import dalvik.annotation.optimization.FastNative;

import com.facebook.jni.HybridData;
import com.facebook.proguard.annotations.DoNotStrip;

/**
 * A Runnable that has a native run implementation.
 */
@DoNotStrip
public class NativeRunnable implements Runnable {

  private final HybridData mHybridData;

  @DoNotStrip
  private NativeRunnable(HybridData hybridData) {
    mHybridData = hybridData;
  }

  @FastNative
  public native void run();
}
