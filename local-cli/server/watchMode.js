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
  const cmds = [
    {
      key: chalk.bold.cyan('r'),
      desc: `${chalk.bold.cyan('reload')} the app`
    },
    {
      key: chalk.bold.cyan('d'),
      desc: `${chalk.bold.cyan('open')} developer menu`
    },
    {
      key: chalk.bold.magenta('t'),
      desc: `${chalk.bold.magenta('reverse')} the port 8081`
    },
    {
      key: chalk.bold.green('i'),
      desc: `${chalk.bold.green('install')} the app`
    },
    {
      key: chalk.bold.green('a'),
      desc: `${chalk.bold.green('launch')} the app`
    },
    {
      key: chalk.bold.red('s'),
      desc: `${chalk.bold.red('stop')} the app`
    },
    {
      key: chalk.bold.yellow('c'),
      desc: `${chalk.bold.yellow('clear screen')}`
    },
    {
      key: chalk.bold.yellow('h'),
      desc: `${chalk.bold.yellow('help')}`
    },
  ]
  console.log(
    '\n\n' + cmds.map(({ key, desc }) => `${key} - ${desc}`).join('\n')
  )
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
      console.info('Try to reverse localhost:8081');
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
      console.info('Starting...');
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
      console.info('Stop...');
      try {
        execSync('adb shell am force-stop com.kuss.rewind', {
          stdio: 'inherit',
        })
        console.info('Stopped');
      } catch (e) {
        e.message && console.info(e.message);
        console.log()
      }
    } else if (name === 'i') {
      console.info('Installing...');
      try {
        execSync('npx react-native run-android --no-packager', {
          stdio: 'inherit',
        })
        console.info('Installed');
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
