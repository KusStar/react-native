// Copyright (c) 2004-present, Facebook, Inc.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.react.bridge;

import dalvik.annotation.optimization.FastNative;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.facebook.common.logging.FLog;
import com.facebook.jni.HybridData;
import com.facebook.proguard.annotations.DoNotStrip;
import com.facebook.react.common.ReactConstants;

@DoNotStrip
public class Inspector {
  static {
    ReactBridge.staticInit();
  }

  private final HybridData mHybridData;

  public static List<Page> getPages() {
    try {
      return Arrays.asList(instance().getPagesNative());
    } catch (UnsatisfiedLinkError e) {
      FLog.e(ReactConstants.TAG, "Inspector doesn't work in open source yet", e);
      return Collections.emptyList();
    }
  }

  public static LocalConnection connect(int pageId, RemoteConnection remote) {
    try {
      return instance().connectNative(pageId, remote);
    } catch (UnsatisfiedLinkError e) {
      FLog.e(ReactConstants.TAG, "Inspector doesn't work in open source yet", e);
      throw new RuntimeException(e);
    }
  }

  @FastNative
  private static native Inspector instance();

  @FastNative
  private native Page[] getPagesNative();

  @FastNative
  private native LocalConnection connectNative(int pageId, RemoteConnection remote);

  private Inspector(HybridData hybridData) {
    mHybridData = hybridData;
  }

  @DoNotStrip
  public static class Page {
    private final int mId;
    private final String mTitle;
    private final String mVM;

    public int getId() {
      return mId;
    }

    public String getTitle() {
      return mTitle;
    }

    public String getVM() {
      return mVM;
    }

    @Override
    public String toString() {
      return "Page{" +
          "mId=" + mId +
          ", mTitle='" + mTitle + '\'' +
          '}';
    }

    @DoNotStrip
    private Page(int id, String title, String vm) {
      mId = id;
      mTitle = title;
      mVM = vm;
    }
  }

  @DoNotStrip
  public interface RemoteConnection {
    void onMessage(String message);
    void onDisconnect();
  }

  @DoNotStrip
  public static class LocalConnection {
    private final HybridData mHybridData;

    @FastNative
    public native void sendMessage(String message);
    @FastNative
    public native void disconnect();

    private LocalConnection(HybridData hybridData) {
      mHybridData = hybridData;
    }
  }
}
