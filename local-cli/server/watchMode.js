"use strict";
const { execSync } = require('child_process');

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;

function _readline() {
  const data = _interopRequireDefault(require("readline"));

  _readline = function () {
    return data;
  };

  return data;
}

function _chalk() {
  const data = _interopRequireDefault(require("chalk"));

  _chalk = function () {
    return data;
  };

  return data;
}

var _hookStdout = _interopRequireDefault(require("./hookStdout"));

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function printWatchModeInstructions() {
  const chalk = _chalk().default
  console.log(
    `\n\nTo ${chalk.bold.cyan('reload')} the app press`, chalk.bold.cyan('"r"'),
    `\nTo ${chalk.bold.cyan('open')} developer menu press`, chalk.bold.cyan('"d"'),
    `\nTo ${chalk.bold.magenta('reverse')} the port 8081, press`, chalk.bold.magenta('"t"'),
    `\nTo ${chalk.bold.green('launch')} the app, press`, chalk.bold.green('"a"'),
    `\nTo ${chalk.bold.red('stop')} the app, press`, chalk.bold.red('"s"'),
    `\nTo ${chalk.bold.yellow('clear screen')}, press`, chalk.bold.cyan('"c"'),
    `\nTo print the ${chalk.bold.yellow('help')}, press`, chalk.bold.yellow('"h"'),
  );
}

function enableWatchMode(messageSocket) {
  // We need to set this to true to catch key presses individually.
  // As a result we have to implement our own method for exiting
  // and other commands (e.g. ctrl+c & ctrl+z)
  if (!process.stdin.setRawMode) {
    console.debug('Watch mode is not supported in this environment');

    return;
  }

  _readline().default.emitKeypressEvents(process.stdin);

  process.stdin.setRawMode(true); // We have no way of knowing when the dependency graph is done loading
  // except by hooking into stdout itself. We want to print instructions
  // right after its done loading.

  const restore = (0, _hookStdout.default)(output => {
    if (output.includes('Loading dependency graph, done')) {
      printWatchModeInstructions();
      restore();
    }
  });
  process.stdin.on('keypress', (_key, data) => {
    const {
      ctrl,
      name
    } = data;

    if (ctrl === true) {
      switch (name) {
        case 'c':
          process.exit();
          break;

        case 'z':
          process.emit('SIGTSTP');
          break;
      }
    } else if (name === 'r') {
      messageSocket.broadcast('reload', null);
      console.info('Reloading app...');
    } else if (name === 'd') {
      messageSocket.broadcast('devMenu', null);
      console.info('Opening developer menu...');
    } else if (name === 't') {
      try {
        execSync('adb reverse tcp:8081 tcp:8081', {
          stdio: 'inherit',
        })
        console.info('Reversed');
      } catch (e) {
        e.message && console.info(e.message);
        console.log()
      }
    } else if (name === 'a') {
      try {
        execSync('adb shell am start -n com.kuss.rewind/.MainActivity', {
          stdio: 'inherit',
        })
        console.info('Started');
      } catch (e) {
        e.message && console.info(e.message);
        console.log()
      }
    } else if (name === 's') {
      try {
        execSync('adb shell am force-stop com.kuss.rewind', {
          stdio: 'inherit',
        })
        console.info('Stopped');
      } catch (e) {
        e.message && console.info(e.message);
        console.log()
      }
    } else if (name === 'c') {
      console.clear()
    } else if (name === 'h') {
      console.clear()
      printWatchModeInstructions()
      console.log()
    }
  });
}

var _default = enableWatchMode;
exports.default = _default;

//# sourceMappingURL=watchMode.js.map
