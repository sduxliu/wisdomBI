import { defineConfig } from '@umijs/max';
import routes from './config/routes';
import MonacoEditorWebpackPlugin from "monaco-editor-webpack-plugin";

export default defineConfig({
  antd: {},
  access: {},
  model: {},
  initialState: {},
  request: {},
  layout: {},
  headScripts: [
    // 解决首次加载时白屏的问题
    {
      src: '/scripts/loading.js',
      async: true,
    },
  ],
  presets: [ 'umi-presets-pro' ],
  openAPI: [
    {
      requestLibPath: "import { request } from '@umijs/max'",
      // 或者使用在线的版本
      schemaPath: "http://localhost:8102/api/v2/api-docs",
      projectName: 'wisdomBI',
      mock: false,
    },
  ],
  // @ts-ignore
  // mock: true,
  routes,
  npmClient: 'yarn',
  // https://umijs.org/zh-CN/guide/boost-compile-speed#monaco-editor-%E7%BC%96%E8%BE%91%E5%99%A8%E6%89%93%E5%8C%85
  // @ts-ignore
      chainWebpack(config) {
        config.module
          .rule('svg')
          .test(/\.svg$/)
          .use('svgr')
          .loader('@svgr/webpack');
      },
  });

