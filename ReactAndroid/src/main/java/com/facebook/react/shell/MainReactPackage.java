/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.react.shell;

import com.facebook.react.LazyReactPackage;
import com.facebook.react.animated.NativeAnimatedModule;
import com.facebook.react.bridge.ModuleSpec;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.module.model.ReactModuleInfoProvider;
import com.facebook.react.modules.accessibilityinfo.AccessibilityInfoModule;
import com.facebook.react.modules.appearance.AppearanceModule;
import com.facebook.react.modules.appstate.AppStateModule;
import com.facebook.react.modules.blob.BlobModule;
import com.facebook.react.modules.blob.FileReaderModule;
import com.facebook.react.modules.clipboard.ClipboardModule;
import com.facebook.react.modules.dialog.DialogModule;
import com.facebook.react.modules.fresco.FrescoModule;
import com.facebook.react.modules.i18nmanager.I18nManagerModule;
import com.facebook.react.modules.image.ImageLoaderModule;
import com.facebook.react.modules.intent.IntentModule;
import com.facebook.react.modules.netinfo.NetInfoModule;
import com.facebook.react.modules.network.NetworkingModule;
import com.facebook.react.modules.permissions.PermissionsModule;
import com.facebook.react.modules.share.ShareModule;
import com.facebook.react.modules.statusbar.StatusBarModule;
import com.facebook.react.modules.storage.AsyncStorageModule;
import com.facebook.react.modules.toast.ToastModule;
import com.facebook.react.modules.vibration.VibrationModule;
import com.facebook.react.modules.websocket.WebSocketModule;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.views.drawer.ReactDrawerLayoutManager;
import com.facebook.react.views.image.ReactImageManager;
import com.facebook.react.views.modal.ReactModalHostManager;
import com.facebook.react.views.progressbar.ReactProgressBarViewManager;
import com.facebook.react.views.scroll.ReactHorizontalScrollContainerViewManager;
import com.facebook.react.views.scroll.ReactHorizontalScrollViewManager;
import com.facebook.react.views.scroll.ReactScrollViewManager;
import com.facebook.react.views.swiperefresh.SwipeRefreshLayoutManager;
import com.facebook.react.views.switchview.ReactSwitchManager;
import com.facebook.react.views.text.ReactRawTextManager;
import com.facebook.react.views.text.ReactTextViewManager;
import com.facebook.react.views.text.ReactVirtualTextViewManager;
import com.facebook.react.views.text.frescosupport.FrescoBasedReactTextInlineImageViewManager;
import com.facebook.react.views.textinput.ReactTextInputManager;
import com.facebook.react.views.view.ReactViewManager;
import com.facebook.react.views.viewpager.ReactViewPagerManager;
import com.facebook.react.views.webview.ReactWebViewManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Provider;

/**
 * Package defining basic modules and view managers.
 */
public class MainReactPackage extends LazyReactPackage {

  private MainPackageConfig mConfig;

  public MainReactPackage() {
  }

  /**
   * Create a new package with configuration
   */
  public MainReactPackage(MainPackageConfig config) {
    mConfig = config;
  }

  @Override
  public List<ModuleSpec> getNativeModules(final ReactApplicationContext context) {
    return Arrays.asList(
        ModuleSpec.nativeModuleSpec(
            AccessibilityInfoModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new AccessibilityInfoModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            AppearanceModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new AppearanceModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            AppStateModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new AppStateModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            BlobModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new BlobModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            FileReaderModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new FileReaderModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            AsyncStorageModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new AsyncStorageModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            ClipboardModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new ClipboardModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            DialogModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new DialogModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            FrescoModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new FrescoModule(
                    context, true, mConfig != null ? mConfig.getFrescoConfig() : null);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            I18nManagerModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new I18nManagerModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            ImageLoaderModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new ImageLoaderModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            IntentModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new IntentModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            NativeAnimatedModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new NativeAnimatedModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            NetworkingModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new NetworkingModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            NetInfoModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new NetInfoModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            PermissionsModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new PermissionsModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            ShareModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new ShareModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            StatusBarModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new StatusBarModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            ToastModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new ToastModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            VibrationModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new VibrationModule(context);
              }
            }),
        ModuleSpec.nativeModuleSpec(
            WebSocketModule.class,
            new Provider<NativeModule>() {
              @Override
              public NativeModule get() {
                return new WebSocketModule(context);
              }
            }));
  }

  @Override
  public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
    List<ViewManager> viewManagers = new ArrayList<>();

    viewManagers.add(new ReactDrawerLayoutManager());
    viewManagers.add(new ReactHorizontalScrollViewManager());
    viewManagers.add(new ReactHorizontalScrollContainerViewManager());
    viewManagers.add(new ReactScrollViewManager());
    viewManagers.add(new ReactSwitchManager());
    viewManagers.add(new ReactWebViewManager());
    viewManagers.add(new SwipeRefreshLayoutManager());

    // Native equivalents
    viewManagers.add(new FrescoBasedReactTextInlineImageViewManager());
    viewManagers.add(new ReactImageManager());
    viewManagers.add(new ReactModalHostManager());
    viewManagers.add(new ReactRawTextManager());
    viewManagers.add(new ReactTextInputManager());
    viewManagers.add(new ReactTextViewManager());
    viewManagers.add(new ReactViewManager());
    viewManagers.add(new ReactViewPagerManager());
    viewManagers.add(new ReactVirtualTextViewManager());
    viewManagers.add(new ReactProgressBarViewManager());

    return viewManagers;
  }

  @Override
  public ReactModuleInfoProvider getReactModuleInfoProvider() {
    // This has to be done via reflection or we break open source.
    return LazyReactPackage.getReactModuleInfoProviderViaReflection(this);
  }
}
