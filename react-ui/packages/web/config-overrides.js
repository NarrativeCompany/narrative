const tsImportPluginFactory = require('ts-import-plugin');
const { getLoader, loaderNameMatches } = require('react-app-rewired');
const { override } = require('customize-cra');
const { createTransformer } = require('typescript-plugin-styled-components');
const rewireStyledComponents = require("react-app-rewire-styled-components");

const tsLoaderMatcher = rule => loaderNameMatches(rule, 'ts-loader');
const getTsLoader = rules => getLoader(rules, tsLoaderMatcher);

const rewireTsCustomTransformers = (config, env) => {
  const tsLoader = getTsLoader(config.module.rules);

  const beforeTransformers = [
    tsImportPluginFactory({
      libraryDirectory: 'es',
      libraryName: 'antd',
      style: 'css',
    })
  ];

  // on development environments, we'll push the transformer that does the styled-components debugging
  if(env === 'development') {
    // based loosely off:
    // https://github.com/AustinBrunkhorst/react-app-rewire-styled-components-typescript/blob/master/src/index.js
    // could have used that dependency, except it doesn't use the 1.0.0 version of typescript-plugin-styled-components
    // we can customize getDisplayName in createTransformer, but the default works for now.
    beforeTransformers.push(createTransformer());
  }

  tsLoader.options = {
    getCustomTransformers: () => ({
      before: beforeTransformers
    })
  };

  return config;
};

const removeInlineFileLoader = (config, env) => {
  // bl: this references configs from webpack.config.prod.js / webpack.config.dev.js from react-scripts-ts
  const loaders = config.module.rules.find(rule => Array.isArray(rule.oneOf)).oneOf;
  loaders.forEach(loader => {
    // bl: remove the inline (URL) file loader. this loader has a limit of 10000 so any file smaller than that will
    // be embedded in javascript and included inline with a base64 encoded data URL.
    // that's particularly problematic for prerender, which results in much larger HTML files than necessary.
    // i understand the premise of the feature, but i just don't see much of an issue in making requests for most
    // assets. we could remove the files here altogether, but for now, i'm just going to reduce the limit
    // https://github.com/wmonk/create-react-app-typescript/blob/master/config/webpack.config.prod.js#L154
    if (loader.loader && loader.loader.includes('url-loader')) {
      // bl: instead of removing the loader altogether, let's just drop the limit way down
      // loaders.splice(index, 1);
      // bl: anything 512 bytes (0.5KB) or less can be inlined. that seems reasonable enough.
      loader.options.limit = 512;
    }
  });
  return config;
};

module.exports = override(
  rewireTsCustomTransformers,
  rewireStyledComponents,
  removeInlineFileLoader
);
