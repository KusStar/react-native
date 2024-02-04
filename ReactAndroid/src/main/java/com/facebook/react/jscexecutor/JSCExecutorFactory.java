/*
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.jscexecutor;

import android.util.Log;

import java.lang.reflect.Field;

import com.facebook.react.bridge.JavaScriptExecutor;
import com.facebook.react.bridge.JavaScriptExecutorFactory;
import com.facebook.react.bridge.WritableNativeMap;

public class JSCExecutorFactory implements JavaScriptExecutorFactory {
  private final String mAppName;
  private final String mDeviceName;

  public JSCExecutorFactory(String appName, String deviceName) {
    this.mAppName = appName;
    this.mDeviceName = deviceName;
  }

  @Override
  public JavaScriptExecutor create() throws Exception {
    WritableNativeMap jscConfig = new WritableNativeMap();
    jscConfig.putString("OwnerIdentity", "ReactNative");
    jscConfig.putString("AppIdentity", mAppName);
    jscConfig.putString("DeviceIdentity", mDeviceName);

    boolean isDebug = false;
    try {
        Class<?> clazz = Class.forName("com.kuss.rewind.BuildConfig");
        Field field = clazz.getField("DEBUG");
        isDebug = (boolean) field.get(null);
    } catch (Exception e) {
        e.printStackTrace();
    }

    Log.i("JSCExecutorFactory", "isDebug: " + isDebug);

    jscConfig.putBoolean("isDebug", isDebug);

    return new JSCExecutor(jscConfig);
  }

  @Override
  public void startSamplingProfiler() {
    throw new UnsupportedOperationException(
        "Starting sampling profiler not supported on " + toString());
  }

  @Override
  public void stopSamplingProfiler(String filename) {
    throw new UnsupportedOperationException(
        "Stopping sampling profiler not supported on " + toString());
  }

  @Override
  public String toString() {
    return "JSIExecutor+JSCRuntime";
  }
}
