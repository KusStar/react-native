/**
 * Copyright (c) 2016-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @format
 * @flow
 */

'use strict';

/* $FlowFixMe(>=0.54.0 site=react_native_oss) This comment suppresses an error
 * found when Flow v0.54 was deployed. To see the error delete this comment and
 * run Flow. */
const Promise = require('fbjs/lib/Promise.native');

if (__DEV__ || global._isDebug) {
  /* $FlowFixMe(>=0.54.0 site=react_native_oss) This comment suppresses an
   * error found when Flow v0.54 was deployed. To see the error delete this
   * comment and run Flow. */
  require('promise/setimmediate/rejection-tracking').enable({
    allRejections: true,
    onUnhandled: (id, error = {}) => {
      let message: string;

      const stringValue = Object.prototype.toString.call(error);
      if (stringValue === '[object Error]') {
        message = Error.prototype.toString.call(error);
      } else {
        /* $FlowFixMe(>=0.54.0 site=react_native_oss) This comment suppresses
         * an error found when Flow v0.54 was deployed. To see the error delete
         * this comment and run Flow. */
        message = require('pretty-format')(error);
      }

      const parseErrorStack = require('parseErrorStack');
      const stack = parseErrorStack(error);

      if (!stack) {
        const warning =
          `Possible Unhandled Promise Rejection (id: ${id}):\n` +
          `${message}\n`
        console.warn(warning);
        return
      }

      const symbolicateStackTrace = require('symbolicateStackTrace');
      symbolicateStackTrace(stack)
        .then(prettyStack => {
          if (prettyStack) {
            const stackLines = prettyStack
              .map(stack => {
                const file = stack.file || '';
                const lineNumber = stack.lineNumber || '';
                const column = stack.column || '';
                const sep1 = file && lineNumber ? ':' : ''
                const sep2 = lineNumber && column ? ':' : ''
                const extra = `${file}${sep1}${lineNumber}${sep2}${column}`
                return `at ${stack.methodName} ${extra ? ' (' + extra + ')' : ''}`
              })
              .join('\n\t')
            const warning =
              `Possible Unhandled Promise Rejection (id: ${id}):\n    ` +
              `${message}\n\t` +
              (stackLines);
            console.warn(warning);
          } else {
            const warning =
              `Possible Unhandled Promise Rejection (id: ${id}):\n` +
              `${message}\n` +
              (error.stack == null ? '' : error.stack);
            console.warn(warning);
          }
        })
        .catch((e) => {
          throw e;
        })
    },
    onHandled: id => {
      const warning =
        `Promise Rejection Handled (id: ${id})\n` +
        'This means you can ignore any previous messages of the form ' +
        `"Possible Unhandled Promise Rejection (id: ${id}):"`;
      console.warn(warning);
    },
  });
}

module.exports = Promise;
