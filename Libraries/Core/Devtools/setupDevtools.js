/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @format
 * @flow
 */

'use strict';

type DevToolsPluginConnection = {
  isAppActive: () => boolean,
  host: string,
  port: number,
};

type DevToolsPlugin = {
  connectToDevTools: (connection: DevToolsPluginConnection) => void,
};

let register = function() {
  // noop
};

// removed react-devtools-core

module.exports = {
  register,
};
