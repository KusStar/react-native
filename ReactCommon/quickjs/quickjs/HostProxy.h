#pragma once

#include "QuickJSRuntime.h"
#include "jsi/jsi.h"

namespace qjs {

struct OpaqueData {
  static void *GetHostData(JSValueConst this_val);

  void *hostData_;
};

class OpaqueOwner {
  public:
  virtual OpaqueData *GetOpaqueData() = 0;
  virtual ~OpaqueOwner() = default;
};

class HostObjectProxy : public OpaqueOwner {
 public:
  HostObjectProxy(
      QuickJSRuntime &runtime,
      std::shared_ptr<jsi::HostObject> hostObject);
  ~HostObjectProxy() {};

  std::shared_ptr<jsi::HostObject> GetHostObject();

  OpaqueData *GetOpaqueData() override;

  static JSClassID GetClassID();

  static JSValue Getter(JSContext *ctx, JSValueConst this_val, JSAtom name);

  static JSValue
  Setter(JSContext *ctx, JSValueConst this_val, JSAtom name, JSValue val);

  static JSValue Enumerator(JSContext *ctx, JSValueConst this_val);

  static void Finalizer(JSRuntime *rt, JSValue val);

  static JSClassDef kJSClassDef;
  static const JSCFunctionListEntry kTemplateInterceptor[];

 private:
  QuickJSRuntime &runtime_;
  std::shared_ptr<jsi::HostObject> hostObject_;
  OpaqueData opaqueData_;

  static JSClassID kJSClassID;
};

class HostFunctionProxy : public OpaqueOwner {
 public:
  HostFunctionProxy(
      QuickJSRuntime &runtime,
      jsi::HostFunctionType &&hostFunction);
    
  ~HostFunctionProxy() {};

  jsi::HostFunctionType &GetHostFunction();

  OpaqueData *GetOpaqueData() override;

  static JSClassID GetClassID();

  static void Finalizer(JSRuntime *rt, JSValue val);

  static JSValue FunctionCallback(
      JSContext *ctx,
      JSValueConst func_obj,
      JSValueConst val,
      int argc,
      JSValueConst *argv,
      int flags);

  static JSClassDef kJSClassDef;

 private:
  QuickJSRuntime &runtime_;
  jsi::HostFunctionType hostFunction_;
  OpaqueData opaqueData_;

  static JSClassID kJSClassID;
};

} // namespace qjs
