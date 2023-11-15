#include <jsi/jsi.h>
#include <cxxreact/CallInvoker.h>

using namespace facebook;

namespace ops {
void install(jsi::Runtime &rt, std::shared_ptr<react::CallInvoker> invoker, const char *docPath);
}

