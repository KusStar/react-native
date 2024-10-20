/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */

package com.facebook.react.jscexecutor;

import dalvik.annotation.optimization.FastNative;

import com.facebook.jni.HybridData;
import com.facebook.proguard.annotations.DoNotStrip;
import com.facebook.react.bridge.JavaScriptExecutor;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.soloader.SoLoader;

@DoNotStrip
/* package */ class JSCExecutor extends JavaScriptExecutor {
  static {
    SoLoader.loadLibrary("jscexecutor");
  }

  /* package */ JSCExecutor(ReadableNativeMap jscConfig) {
    super(initHybrid(jscConfig));
  }

  @Override
  public String getName() {
    return "JSCExecutor";
  }

  @FastNative
  private static native HybridData initHybrid(ReadableNativeMap jscConfig);
}
