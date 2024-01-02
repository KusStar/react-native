// Copyright (c) 2004-present, Facebook, Inc.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

#pragma once


#include <condition_variable>
#include <list>
#include <memory>
#include <mutex>

#include <ReactCommon/CallInvoker.h>
#include <cxxreact/NativeToJsBridge.h>

#ifndef RN_EXPORT
#define RN_EXPORT __attribute__((visibility("default")))
#endif

namespace folly {
struct dynamic;
}

namespace facebook {
namespace react {

class JSBigString;
class JSExecutorFactory;
class MessageQueueThread;
class ModuleRegistry;
class RAMBundleRegistry;

struct InstanceCallback {
  virtual ~InstanceCallback() {}
  virtual void onBatchComplete() {}
  virtual void incrementPendingJSCalls() {}
  virtual void decrementPendingJSCalls() {}
};

class RN_EXPORT Instance {
public:
  ~Instance();
  void initializeBridge(std::unique_ptr<InstanceCallback> callback,
                        std::shared_ptr<JSExecutorFactory> jsef,
                        std::shared_ptr<MessageQueueThread> jsQueue,
                        std::shared_ptr<ModuleRegistry> moduleRegistry);
          
  void setSourceURL(std::string sourceURL);

  void loadScriptFromString(std::unique_ptr<const JSBigString> string,
                            std::string sourceURL, bool loadSynchronously);
  static bool isIndexedRAMBundle(const char *sourcePath);
  void loadRAMBundleFromFile(const std::string& sourcePath,
                             const std::string& sourceURL,
                             bool loadSynchronously);
  void loadRAMBundle(std::unique_ptr<RAMBundleRegistry> bundleRegistry,
                     std::unique_ptr<const JSBigString> startupScript,
                     std::string startupScriptSourceURL, bool loadSynchronously);
  bool supportsProfiling();
  void setGlobalVariable(std::string propName,
                         std::unique_ptr<const JSBigString> jsonValue);
  void *getJavaScriptContext();
  bool isInspectable();
  void callJSFunction(std::string &&module, std::string &&method,
                      folly::dynamic &&params);
  void callJSCallback(uint64_t callbackId, folly::dynamic &&params);

  // This method is experimental, and may be modified or removed.
  void registerBundle(uint32_t bundleId, const std::string& bundlePath);

  const ModuleRegistry &getModuleRegistry() const;
  ModuleRegistry &getModuleRegistry();

  void handleMemoryPressure(int pressureLevel);

  std::shared_ptr<CallInvoker> getJSCallInvoker();

private:
  void callNativeModules(folly::dynamic &&calls, bool isEndOfBatch);
  void loadApplication(std::unique_ptr<RAMBundleRegistry> bundleRegistry,
                       std::unique_ptr<const JSBigString> startupScript,
                       std::string startupScriptSourceURL);
  void loadApplicationSync(std::unique_ptr<RAMBundleRegistry> bundleRegistry,
                           std::unique_ptr<const JSBigString> startupScript,
                           std::string startupScriptSourceURL);

  std::shared_ptr<InstanceCallback> callback_;
  std::shared_ptr<NativeToJsBridge> nativeToJsBridge_;
  std::shared_ptr<ModuleRegistry> moduleRegistry_;

  std::mutex m_syncMutex;
  std::condition_variable m_syncCV;
  bool m_syncReady = false;

  // NOTE: from https://github.com/facebook/react-native/blob/de566494302630c8a7508268870323e2553968e1/ReactCommon/cxxreact/Instance.h#LL103C3-L103C51

  class JSCallInvoker : public CallInvoker {
   private:
    std::weak_ptr<NativeToJsBridge> m_nativeToJsBridge;
    std::mutex m_mutex;
    bool m_shouldBuffer = true;
    std::list<std::function<void()>> m_workBuffer;

    void scheduleAsync(std::function<void()> &&work);

   public:
    void setNativeToJsBridgeAndFlushCalls(
        std::weak_ptr<NativeToJsBridge> nativeToJsBridge);
    void invokeAsync(std::function<void()> &&work) override;
    void invokeSync(std::function<void()> &&work) override;
  };

  std::shared_ptr<JSCallInvoker> jsCallInvoker_ =
      std::make_shared<JSCallInvoker>();
};

} // namespace react
} // namespace facebook
