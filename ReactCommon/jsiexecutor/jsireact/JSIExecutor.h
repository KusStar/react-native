// Copyright 2004-present Facebook. All Rights Reserved.

#pragma once

#include "JSINativeModules.h"
// NOTE: from react-native-quick-sqlite
#include "opsqlite/bindings.h"

#include <ReactCommon/CallInvoker.h>
#include <cxxreact/JSBigString.h>
#include <cxxreact/JSExecutor.h>
#include <cxxreact/RAMBundleRegistry.h>
#include <jsi/jsi.h>
#include <functional>
#include <mutex>

namespace facebook {
namespace react {

// A JSIScopedTimeoutInvoker is a trampoline-type function for introducing
// timeouts. Call the TimeoutInvoker with a function to execute, the invokee.
// The TimeoutInvoker will immediately invoke it, synchronously on the same
// thread. If the invokee fails to return after some timeout (private to the
// TimeoutInvoker), a soft error may be reported.
//
// If a soft error is reported, the second parameter errorMessageProducer will
// be invoked to produce an error message, which will be included in the soft
// error report. Note that the errorMessageProducer will be invoked
// asynchronously on a different thread.
//
// The timeout behavior does NOT caues the invokee to aborted. If the invokee
// blocks forever, so will the ScopedTimeoutInvoker (but the soft error may
// still be reported).
//
// The invokee is passed by const ref because it is executed synchronously, but
// the errorMessageProducer is passed by value because it must be copied or
// moved for async execution.
//
// Example usage:
//
//   int param = ...;
//   timeoutInvoker(
//       [&]{ someBigWork(param); },
//       [=] -> std::string {
//           return "someBigWork, param " + std::to_string(param);
//       })
//
using JSIScopedTimeoutInvoker = std::function<void(
    const std::function<void()>& invokee,
    std::function<std::string()> errorMessageProducer)>;

class BigStringBuffer : public jsi::Buffer {
 public:
  BigStringBuffer(std::unique_ptr<const JSBigString> script)
      : script_(std::move(script)) {}

  size_t size() const override {
    return script_->size();
  }

  const uint8_t* data() const override {
    return reinterpret_cast<const uint8_t*>(script_->c_str());
  }

 private:
  std::unique_ptr<const JSBigString> script_;
};

class JSIExecutor : public JSExecutor {
 public:
  using Logger =
      std::function<void(const std::string& message, unsigned int logLevel)>;

  using RuntimeInstaller = std::function<void(jsi::Runtime& runtime)>;

  JSIExecutor(
      std::shared_ptr<jsi::Runtime> runtime,
      std::shared_ptr<ExecutorDelegate> delegate,
      Logger logger,
      const JSIScopedTimeoutInvoker& timeoutInvoker,
      RuntimeInstaller runtimeInstaller);
  void loadApplicationScript(
      std::shared_ptr<CallInvoker> jsCallInvoker,
      std::unique_ptr<const JSBigString> script,
      std::string sourceURL) override;
  void setBundleRegistry(std::unique_ptr<RAMBundleRegistry>) override;
  void registerBundle(uint32_t bundleId, const std::string& bundlePath)
      override;
  void callFunction(
      const std::string& moduleId,
      const std::string& methodId,
      const folly::dynamic& arguments) override;
  void invokeCallback(const double callbackId, const folly::dynamic& arguments)
      override;
  void setGlobalVariable(
      std::string propName,
      std::unique_ptr<const JSBigString> jsonValue) override;
  std::string getDescription() override;
  void* getJavaScriptContext() override;
  bool isInspectable() override;
  void handleMemoryPressure(int pressureLevel) override;
  void destroy() override;

  // An implementation of JSIScopedTimeoutInvoker that simply runs the
  // invokee, with no timeout.
  static void defaultTimeoutInvoker(
      const std::function<void()>& invokee,
      std::function<std::string()> errorMessageProducer) {
    (void)errorMessageProducer;
    invokee();
  }

 private:
  class NativeModuleProxy;

  void flush() override;
  void bindBridge();
  void callNativeModules(const jsi::Value& queue, bool isEndOfBatch);
  jsi::Value nativeCallSyncHook(const jsi::Value* args, size_t count);
  jsi::Value nativeRequire(const jsi::Value* args, size_t count);
  jsi::Value globalEvalWithSourceUrl(const jsi::Value *args, size_t count);

  std::shared_ptr<jsi::Runtime> runtime_;
  std::shared_ptr<ExecutorDelegate> delegate_;
  std::shared_ptr<JSINativeModules> nativeModules_;
  std::once_flag bindFlag_;
  std::unique_ptr<RAMBundleRegistry> bundleRegistry_;
  Logger logger_;
  JSIScopedTimeoutInvoker scopedTimeoutInvoker_;
  RuntimeInstaller runtimeInstaller_;

  folly::Optional<jsi::Function> callFunctionReturnFlushedQueue_;
  folly::Optional<jsi::Function> invokeCallbackAndReturnFlushedQueue_;
  folly::Optional<jsi::Function> flushedQueue_;
  folly::Optional<jsi::Function> callFunctionReturnResultAndFlushedQueue_;
};

class JSIExecutorFactory : public JSExecutorFactory {
 public:
  explicit JSIExecutorFactory(
      std::shared_ptr<jsi::Runtime> runtime,
      JSIExecutor::Logger logger,
      JSIExecutor::RuntimeInstaller);

  std::unique_ptr<JSExecutor> createJSExecutor(
      std::shared_ptr<ExecutorDelegate> delegate,
      std::shared_ptr<MessageQueueThread> jsQueue) override;

 private:
  std::shared_ptr<jsi::Runtime> runtime_;
  JSIExecutor::Logger logger_;
  JSIExecutor::RuntimeInstaller runtimeInstaller_;
};

using Logger =
    std::function<void(const std::string &message, unsigned int logLevel)>;
void bindNativeLogger(jsi::Runtime &runtime, Logger logger);

using PerformanceNow = std::function<double()>;
void bindNativePerformanceNow(
    jsi::Runtime &runtime,
    PerformanceNow performanceNow);

} // namespace react
} // namespace facebook
