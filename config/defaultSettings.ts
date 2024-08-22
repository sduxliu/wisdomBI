// import Logo from '../public/logo2.svg';
import {ProLayoutProps} from "@ant-design/pro-layout";
import { ReactComponent as Logo } from '../public/logo2.svg';

const Settings: ProLayoutProps & {
  pwa?: boolean;
  logo?: string;
} = {
  navTheme: 'light',
  colorPrimary: '#1890ff',
  layout: 'mix',
  contentWidth: 'Fluid',
  fixedHeader: false,
  fixSiderbar: true,
  colorWeak: false,
  pwa: true,
  title: '智汇数据平台',
  // logo: Logo, // 本地 SVG 文件
  // 或者使用远程 URL
  // logo: 'https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/logo/logo_BI.svg',
  // logo: require('../public/logo2.svg'), // 使用require语法导入
  iconfontUrl: '',
  token: {},
};

export default Settings;
