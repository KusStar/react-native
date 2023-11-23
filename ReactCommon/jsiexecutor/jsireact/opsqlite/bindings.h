#include <jsi/jsilib.h>
#include <jsi/jsi.h>
#include <cxxreact/CallInvoker.h>

using namespace facebook;

namespace opsqlite {

void install(jsi::Runtime &rt, std::shared_ptr<react::CallInvoker> jsCallInvoker, const char *docPath);
void clearState();

}

