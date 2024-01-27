// Copyright 2004-present Facebook. All Rights Reserved.

#include <fb/fbjni.h>
#include <folly/Memory.h>
#include <quickjs/QuickJSRuntime.h>
#include <jsireact/JSIExecutor.h>
#include <react/jni/JavaScriptExecutorHolder.h>
#include <react/jni/JReactMarker.h>
#include <react/jni/JSLogging.h>
#include <react/jni/ReadableNativeMap.h>

using namespace qjs;

namespace facebook {
namespace react {

namespace {

class QuickJSExecutorFactory : public JSExecutorFactory {
private:
  std::string codeCacheDir_;
  int maxStackSize_ = 0;
public:
  QuickJSExecutorFactory(const std::string& codeCacheDir, int maxStackSize) : codeCacheDir_(codeCacheDir), maxStackSize_(maxStackSize) {}
  std::unique_ptr<JSExecutor> createJSExecutor(
      std::shared_ptr<ExecutorDelegate> delegate,
      std::shared_ptr<MessageQueueThread> jsQueue) override {

    std::shared_ptr<QuickJSRuntime> quickJSRuntime = std::make_shared<QuickJSRuntime>(codeCacheDir_, maxStackSize_);

    return folly::make_unique<JSIExecutor>(
      quickJSRuntime,
      delegate,
      [](const std::string& message, unsigned int logLevel) {
        reactAndroidLoggingHook(message, logLevel);
      },
      JSIExecutor::defaultTimeoutInvoker,
      nullptr);
  }
};
}

class QuickJSExecutorHolder
    : public jni::HybridClass<QuickJSExecutorHolder, JavaScriptExecutorHolder> {
 public:
  static constexpr auto kJavaDescriptor = "Lcom/facebook/react/quickjsexecutor/QuickJSExecutor;";

  static jni::local_ref<jhybriddata> initHybrid(
      jni::alias_ref<jclass>, const std::string &codeCacheDir, int maxStackSize) {
    // This is kind of a weird place for stuff, but there's no other
    // good place for initialization which is specific to JSC on
    // Android.
    JReactMarker::setLogPerfMarkerIfNeeded();
    // TODO mhorowitz T28461666 fill in some missing nice to have glue
    return makeCxxInstance(folly::make_unique<QuickJSExecutorFactory>(codeCacheDir, maxStackSize));
  }

  static void registerNatives() {
    registerHybrid({
      makeNativeMethod("initHybrid", QuickJSExecutorHolder::initHybrid),
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
      facebook::react::QuickJSExecutorHolder::registerNatives();
  });
}
