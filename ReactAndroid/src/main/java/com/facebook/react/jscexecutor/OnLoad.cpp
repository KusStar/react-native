// Copyright 2004-present Facebook. All Rights Reserved.

#include <fb/fbjni.h>
#include <folly/Memory.h>
#include <jsi/JSCRuntime.h>
#include <jsireact/JSIExecutor.h>
#include <react/jni/JavaScriptExecutorHolder.h>
#include <react/jni/JReactMarker.h>
#include <react/jni/JSLogging.h>
#include <react/jni/ReadableNativeMap.h>

namespace facebook {
namespace react {

namespace {

class JSCExecutorFactory : public JSExecutorFactory {
private:
  bool isDebug_ = false;
public:
  JSCExecutorFactory(bool isDebug): isDebug_(isDebug) {}

  std::unique_ptr<JSExecutor> createJSExecutor(
      std::shared_ptr<ExecutorDelegate> delegate,
      std::shared_ptr<MessageQueueThread> jsQueue) override {
    
    return folly::make_unique<JSIExecutor>(
      jsc::makeJSCRuntime(isDebug_),
      delegate,
      [](const std::string& message, unsigned int logLevel) {
        reactAndroidLoggingHook(message, logLevel);
      },
      JSIExecutor::defaultTimeoutInvoker,
      nullptr);
  }
};
}
// This is not like JSCJavaScriptExecutor, which calls JSC directly.  This uses
// JSIExecutor with JSCRuntime.
class JSCExecutorHolder
    : public jni::HybridClass<JSCExecutorHolder, JavaScriptExecutorHolder> {
 public:
  static constexpr auto kJavaDescriptor = "Lcom/facebook/react/jscexecutor/JSCExecutor;";

  static jni::local_ref<jhybriddata> initHybrid(
      jni::alias_ref<jclass>, ReadableNativeMap* jscConfig) {
    // This is kind of a weird place for stuff, but there's no other
    // good place for initialization which is specific to JSC on
    // Android.
    JReactMarker::setLogPerfMarkerIfNeeded();
    bool isDebug = jscConfig->getBooleanKey("isDebug");
    // TODO mhorowitz T28461666 fill in some missing nice to have glue
    return makeCxxInstance(folly::make_unique<JSCExecutorFactory>(isDebug));
  }

  static void registerNatives() {
    registerHybrid({
      makeNativeMethod("initHybrid", JSCExecutorHolder::initHybrid),
    });
  }

 private:
  friend HybridBase;
  using HybridBase::HybridBase;
};

} // namespace react
} // namespace facebook

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
  return facebook::jni::initialize(vm, [] {
      facebook::react::JSCExecutorHolder::registerNatives();
  });
}
