/*
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.quickjsexecutor;

import java.io.File;

import java.lang.reflect.Field;

import com.facebook.common.file.FileUtils;
import com.facebook.react.bridge.JavaScriptExecutor;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.bridge.WritableNativeMap;

public class QuickJSExecutorFactory implements JavaScriptExecutorFactory {
  // eg. getApplication().getCacheDir().getAbsolutePath() + "/qjs"
  private String mCodeCacheDir;
  // internal default is 256 * 1024, 0 means no limit
  private int mMaxStackSize = -1;

  public QuickJSExecutorFactory(final String codeCacheDir, int maxStackSize) {
    mCodeCacheDir = codeCacheDir;
    mMaxStackSize = maxStackSize;
  }

  public QuickJSExecutorFactory(final String codeCacheDir) {
    mCodeCacheDir = codeCacheDir;
  }

  @Override
  public JavaScriptExecutor create() throws Exception {
    try {
      FileUtils.mkdirs(new File(mCodeCacheDir));
    } catch (Exception e) {
      e.printStackTrace();
      // Empty string to avoid using code cache.
      mCodeCacheDir = "";
    }

    return new QuickJSExecutor(mCodeCacheDir, mMaxStackSize);
  }

  @Override
  public String toString() {
    return "JSIExecutor+QuickJSRuntime";
  }
}
