/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @format
 * @flow
 */

'use strict';

const invariant = require('fbjs/lib/invariant');

// Export React, plus some native additions.
const ReactNative = {
  // Components
  get AccessibilityInfo() {
    return require('AccessibilityInfo');
  },
  get Appearance() {
    return require('Appearance');
  },
  get ActivityIndicator() {
    return require('ActivityIndicator');
  },
  get Button() {
    return require('Button');
  },
  get DrawerLayoutAndroid() {
    return require('DrawerLayoutAndroid');
  },
  get FlatList() {
    return require('FlatList');
  },
  get Image() {
    return require('Image');
  },
  get ImageBackground() {
    return require('ImageBackground');
  },
  get InputAccessoryView() {
    return require('InputAccessoryView');
  },
  get KeyboardAvoidingView() {
    return require('KeyboardAvoidingView');
  },
  get ListView() {
    return require('ListView');
  },
  get Modal() {
    return require('Modal');
  },
  get ProgressBarAndroid() {
    return require('ProgressBarAndroid');
  },
  get SafeAreaView() {
    return require('SafeAreaView');
  },
  get ScrollView() {
    return require('ScrollView');
  },
  get SectionList() {
    return require('SectionList');
  },
  get Switch() {
    return require('Switch');
  },
  get RefreshControl() {
    return require('RefreshControl');
  },
  get StatusBar() {
    return require('StatusBar');
  },
  get Text() {
    return require('Text');
  },
  get TextInput() {
    return require('TextInput');
  },
  get ToastAndroid() {
    return require('ToastAndroid');
  },
  get Touchable() {
    return require('Touchable');
  },
  get TouchableHighlight() {
    return require('TouchableHighlight');
  },
  get TouchableNativeFeedback() {
    return require('TouchableNativeFeedback');
  },
  get TouchableOpacity() {
    return require('TouchableOpacity');
  },
  get TouchableWithoutFeedback() {
    return require('TouchableWithoutFeedback');
  },
  get View() {
    return require('View');
  },
  get ViewPagerAndroid() {
    return require('ViewPagerAndroid');
  },
  get VirtualizedList() {
    return require('VirtualizedList');
  },
  get WebView() {
    return require('WebView');
  },

  // APIs
  get Alert() {
    return require('Alert');
  },
  get Animated() {
    return require('Animated');
  },
  get AppRegistry() {
    return require('AppRegistry');
  },
  get AppState() {
    return require('AppState');
  },
  get AsyncStorage() {
    return require('AsyncStorage');
  },
  get BackAndroid() {
    return require('BackAndroid');
  }, // deprecated: use BackHandler instead
  get BackHandler() {
    return require('BackHandler');
  },
  get Clipboard() {
    return require('Clipboard');
  },
  get DeviceInfo() {
    return require('DeviceInfo');
  },
  get Dimensions() {
    return require('Dimensions');
  },
  get Easing() {
    return require('Easing');
  },
  get findNodeHandle() {
    return require('ReactNative').findNodeHandle;
  },
  get I18nManager() {
    return require('I18nManager');
  },
  get InteractionManager() {
    return require('InteractionManager');
  },
  get Keyboard() {
    return require('Keyboard');
  },
  get LayoutAnimation() {
    return require('LayoutAnimation');
  },
  get Linking() {
    return require('Linking');
  },
  get NativeEventEmitter() {
    return require('NativeEventEmitter');
  },
  get NetInfo() {
    return require('NetInfo');
  },
  get PanResponder() {
    return require('PanResponder');
  },
  get PermissionsAndroid() {
    return require('PermissionsAndroid');
  },
  get PixelRatio() {
    return require('PixelRatio');
  },
  get Settings() {
    return require('Settings');
  },
  get Share() {
    return require('Share');
  },
  get StyleSheet() {
    return require('StyleSheet');
  },
  get Systrace() {
    return require('Systrace');
  },
  get TVEventHandler() {
    return require('TVEventHandler');
  },
  get UIManager() {
    return require('UIManager');
  },
  get unstable_batchedUpdates() {
    return require('ReactNative').unstable_batchedUpdates;
  },
  get Vibration() {
    return require('Vibration');
  },
  get YellowBox() {
    return __DEV__ ? require('YellowBox') : null;
  },

  // Plugins
  get DeviceEventEmitter() {
    return require('RCTDeviceEventEmitter');
  },
  get NativeAppEventEmitter() {
    return require('RCTNativeAppEventEmitter');
  },
  get NativeModules() {
    return require('NativeModules');
  },
  get Platform() {
    return require('Platform');
  },
  get processColor() {
    return require('processColor');
  },
  get requireNativeComponent() {
    return require('requireNativeComponent');
  },
  get takeSnapshot() {
    return require('takeSnapshot');
  },

  // Prop Types
  get ColorPropType() {
    return require('ColorPropType');
  },
  get EdgeInsetsPropType() {
    return require('EdgeInsetsPropType');
  },
  get PointPropType() {
    return require('PointPropType');
  },
  get ViewPropTypes() {
    return require('ViewPropTypes');
  },
  get useColorScheme() {
    return require('useColorScheme').default;
  },

  // Deprecated
  get Navigator() {
    invariant(
      false,
      'Navigator is deprecated and has been removed from this package. It can now be installed ' +
      'and imported from `react-native-deprecated-custom-components` instead of `react-native`. ' +
      'Learn about alternative navigation solutions at http://facebook.github.io/react-native/docs/navigation.html',
    );
  },
};

module.exports = ReactNative;
