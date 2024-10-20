/**
 * Copyright (c) 2013-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @format
 */

'use strict';

module.exports = () => [
  require.resolve('./Libraries/polyfills/console.js'),
  require.resolve('./Libraries/polyfills/error-guard.js'),
  require.resolve('./Libraries/polyfills/Object.es7.js'),
];
