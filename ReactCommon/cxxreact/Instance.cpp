// Copyright (c) 2004-present, Facebook, Inc.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

#include "Instance.h"

#include "JSBigString.h"
#include "JSBundleType.h"
#include "JSExecutor.h"
#include "MessageQueueThread.h"
#include "MethodCall.h"
#include "NativeToJsBridge.h"
#include "RAMBundleRegistry.h"
#include "RecoverableError.h"
#include "SystraceSection.h"

#include <cxxreact/JSIndexedRAMBundle.h>
#include <folly/Memory.h>
#include <folly/MoveWrapper.h>
#include <folly/json.h>

#include <glog/logging.h>

#include <condition_variable>
#include <fstream>
#include <mutex>
#include <string>

namespace facebook {
namespace react {

Instance::~Instance() {
  if (nativeToJsBridge_) {
    nativeToJsBridge_->destroy();
  }
}

void Instance::initializeBridge(
    std::unique_ptr<InstanceCallback> callback,
    std::shared_ptr<JSExecutorFactory> jsef,
    std::shared_ptr<MessageQueueThread> jsQueue,
    std::shared_ptr<ModuleRegistry> moduleRegistry) {
  callback_ = std::move(callback);
  moduleRegistry_ = std::move(moduleRegistry);
  jsQueue->runOnQueueSync([this, &jsef, jsQueue]() mutable {
    nativeToJsBridge_ = std::make_shared<NativeToJsBridge>(
        jsef.get(), moduleRegistry_, jsQueue, callback_);

    jsCallInvoker_->setNativeToJsBridgeAndFlushCalls(nativeToJsBridge_);

    nativeToJsBridge_->setJsCallInvoker(jsCallInvoker_);

    std::lock_guard<std::mutex> lock(m_syncMutex);
    m_syncReady = true;
    m_syncCV.notify_all();
  });

  CHECK(nativeToJsBridge_);
}

void Instance::loadApplication(std::unique_ptr<RAMBundleRegistry> bundleRegistry,
                               std::unique_ptr<const JSBigString> string,
                               std::string sourceURL) {
  callback_->incrementPendingJSCalls();
  SystraceSection s("Instance::loadApplication", "sourceURL",
                    sourceURL);
  nativeToJsBridge_->loadApplication(std::move(bundleRegistry), std::move(string),
                                     std::move(sourceURL));
}

void Instance::loadApplicationSync(std::unique_ptr<RAMBundleRegistry> bundleRegistry,
                                   std::unique_ptr<const JSBigString> string,
                                   std::string sourceURL) {
  std::unique_lock<std::mutex> lock(m_syncMutex);
  m_syncCV.wait(lock, [this] { return m_syncReady; });

  SystraceSection s("Instance::loadApplicationSync", "sourceURL",
                    sourceURL);
  nativeToJsBridge_->loadApplicationSync(std::move(bundleRegistry), std::move(string),
                                         std::move(sourceURL));
}

void Instance::setSourceURL(std::string sourceURL) {
  callback_->incrementPendingJSCalls();
  SystraceSection s("Instance::setSourceURL", "sourceURL", sourceURL);

  nativeToJsBridge_->loadApplication(nullptr, nullptr, std::move(sourceURL));
}

void Instance::loadScriptFromString(std::unique_ptr<const JSBigString> string,
                                    std::string sourceURL,
                                    bool loadSynchronously) {
  SystraceSection s("Instance::loadScriptFromString", "sourceURL",
                    sourceURL);
  if (loadSynchronously) {
    loadApplicationSync(nullptr, std::move(string), std::move(sourceURL));
  } else {
    loadApplication(nullptr, std::move(string), std::move(sourceURL));
  }
}

bool Instance::isIndexedRAMBundle(const char *sourcePath) {
  std::ifstream bundle_stream(sourcePath, std::ios_base::in);
  BundleHeader header;

  if (!bundle_stream ||
      !bundle_stream.read(reinterpret_cast<char *>(&header), sizeof(header))) {
    return false;
  }

  return parseTypeFromHeader(header) == ScriptTag::RAMBundle;
}

void Instance::loadRAMBundleFromFile(const std::string& sourcePath,
                           const std::string& sourceURL,
                           bool loadSynchronously) {
    auto bundle = folly::make_unique<JSIndexedRAMBundle>(sourcePath.c_str());
    auto startupScript = bundle->getStartupCode();
    auto registry = RAMBundleRegistry::multipleBundlesRegistry(std::move(bundle), JSIndexedRAMBundle::buildFactory());
    loadRAMBundle(
      std::move(registry),
      std::move(startupScript),
      sourceURL,
      loadSynchronously);
}

void Instance::loadRAMBundle(std::unique_ptr<RAMBundleRegistry> bundleRegistry,
                             std::unique_ptr<const JSBigString> startupScript,
                             std::string startupScriptSourceURL,
                             bool loadSynchronously) {
  if (loadSynchronously) {
    loadApplicationSync(std::move(bundleRegistry), std::move(startupScript),
                        std::move(startupScriptSourceURL));
  } else {
    loadApplication(std::move(bundleRegistry), std::move(startupScript),
                    std::move(startupScriptSourceURL));
  }
}

void Instance::setGlobalVariable(std::string propName,
                                 std::unique_ptr<const JSBigString> jsonValue) {
  nativeToJsBridge_->setGlobalVariable(std::move(propName),
                                       std::move(jsonValue));
}

void *Instance::getJavaScriptContext() {
  return nativeToJsBridge_ ? nativeToJsBridge_->getJavaScriptContext()
                           : nullptr;
}

bool Instance::isInspectable() {
  return nativeToJsBridge_ ? nativeToJsBridge_->isInspectable() : false;
}

void Instance::callJSFunction(std::string &&module, std::string &&method,
                              folly::dynamic &&params) {
  callback_->incrementPendingJSCalls();
  nativeToJsBridge_->callFunction(std::move(module), std::move(method),
                                  std::move(params));
}

void Instance::callJSCallback(uint64_t callbackId, folly::dynamic &&params) {
  SystraceSection s("Instance::callJSCallback");
  callback_->incrementPendingJSCalls();
  nativeToJsBridge_->invokeCallback((double)callbackId, std::move(params));
}

void Instance::registerBundle(uint32_t bundleId, const std::string& bundlePath) {
  nativeToJsBridge_->registerBundle(bundleId, bundlePath);
}

const ModuleRegistry &Instance::getModuleRegistry() const {
  return *moduleRegistry_;
}

ModuleRegistry &Instance::getModuleRegistry() { return *moduleRegistry_; }

void Instance::handleMemoryPressure(int pressureLevel) {
  nativeToJsBridge_->handleMemoryPressure(pressureLevel);
}

std::shared_ptr<CallInvoker> Instance::getJSCallInvoker() {
  return std::static_pointer_cast<CallInvoker>(jsCallInvoker_);
}

void Instance::JSCallInvoker::setNativeToJsBridgeAndFlushCalls(
    std::weak_ptr<NativeToJsBridge> nativeToJsBridge) {
  std::lock_guard<std::mutex> guard(m_mutex);

  m_shouldBuffer = false;
  m_nativeToJsBridge = nativeToJsBridge;
  while (m_workBuffer.size() > 0) {
    scheduleAsync(std::move(m_workBuffer.front()));
    m_workBuffer.pop_front();
  }
}

void Instance::JSCallInvoker::invokeSync(std::function<void()> &&work) {
  // TODO: Replace JS Callinvoker with RuntimeExecutor.
  throw std::runtime_error(
      "Synchronous native -> JS calls are currently not supported.");
}

void Instance::JSCallInvoker::invokeAsync(std::function<void()> &&work) {
  std::lock_guard<std::mutex> guard(m_mutex);

  /**
   * Why is is necessary to queue up async work?
   *
   * 1. TurboModuleManager must be created synchronously after the Instance,
   *    before we load the source code. This is when the NativeModule system
   *    is initialized. RCTDevLoadingView shows bundle download progress.
   * 2. TurboModuleManager requires a JS CallInvoker.
   * 3. The JS CallInvoker requires the NativeToJsBridge, which is created on
   *    the JS thread in Instance::initializeBridge.
   *
   * Therefore, although we don't call invokeAsync before the JS bundle is
   * executed, this buffering is implemented anyways to ensure that work
   * isn't discarded.
   */
  if (m_shouldBuffer) {
    m_workBuffer.push_back(std::move(work));
    return;
  }

  scheduleAsync(std::move(work));
}

void Instance::JSCallInvoker::scheduleAsync(std::function<void()> &&work) {
  if (auto strongNativeToJsBridge = m_nativeToJsBridge.lock()) {
    strongNativeToJsBridge->runOnExecutorQueue(
        [work = std::move(work)](JSExecutor *executor) {
          work();
          executor->flush();
        });
  }
}

} // namespace react
} // namespace facebook
