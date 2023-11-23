// Copyright 2004-present Facebook. All Rights Reserved.

#pragma once

#include <jsi/jsi.h>
#include <memory.h>

namespace facebook {
namespace jsc {
static bool BUILD_CONFIG_DEBUG = false;

std::unique_ptr<jsi::Runtime> makeJSCRuntime(bool isDebug);

} // namespace jsc
} // namespace facebook
