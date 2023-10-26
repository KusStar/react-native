/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * @flow strict-local
 * @format
 */

'use strict';

import * as TurboModuleRegistry from '../TurboModule/TurboModuleRegistry';

export type ColorSchemeName = 'light' | 'dark';

export type AppearancePreferences = {|
  // TODO: (hramos) T52919652 Use ?ColorSchemeName once codegen supports union
  // types.
  /* 'light' | 'dark' */
  colorScheme?: ?string,
|};


export default TurboModuleRegistry.get('Appearance');
