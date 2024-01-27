/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */

package com.facebook.react.quickjsexecutor;

import com.facebook.jni.HybridData;
import com.facebook.proguard.annotations.DoNotStrip;
import com.facebook.react.bridge.JavaScriptExecutor;
import com.facebook.soloader.SoLoader;

@DoNotStrip
/* package */ class QuickJSExecutor extends JavaScriptExecutor {
  static {
    SoLoader.loadLibrary("quickjsexecutor");
  }

  /* package */ QuickJSExecutor(final String codeCacheDir, int maxStackSize) {
    super(initHybrid(codeCacheDir, maxStackSize));
  }

  @Override
  public String getName() {
    return "QuickJSExecutor";
  }

  private static native HybridData initHybrid(final String codeCacheDir, int maxStackSize);
}
