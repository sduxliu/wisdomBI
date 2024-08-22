import Footer from '@/components/Footer';
import { Question } from '@/components/RightContent';
import {getLoginUserUsingGet, updatePasswordUsingPost, userLogoutUsingPost} from '@/services/wisdomBI/userController';
import {
  AntDesignOutlined,
  EditOutlined,
  EditTwoTone,
  InfoCircleTwoTone,
  LinkOutlined,
  LogoutOutlined
} from '@ant-design/icons';
import type { RunTimeLayoutConfig } from '@umijs/max';
import { history, Link } from '@umijs/max';
import { AvatarDropdown, AvatarName } from './components/RightContent/AvatarDropdown';
import { errorConfig } from './requestConfig';
import {Avatar, Dropdown} from "antd";
import { useNavigate } from 'react-router-dom';
import NoAuth from "@/pages/403";
import {useEffect, useState} from "react";
import UpdatePasswordForm from '@/pages/User/EditPassword/index';
import Logo from '../public/logo2.svg';
import logo from "@@/plugin-layout/Logo";
const isDev = process.env.NODE_ENV === 'development';
const loginPath = '/user/login';
let userAvatarUrl = '';
let userName = '';

/**
 * @see  https://umijs.org/zh-CN/plugins/plugin-initial-state
 * */
export async function getInitialState(): Promise<{
  currentUser?: API.LoginUserVO;
}> {
  const fetchUserInfo = async () => {
    try {
      const res = await getLoginUserUsingGet();
      if (res.data) {
      // @ts-ignore
        userAvatarUrl = res.data.userAvatar;
        // @ts-ignore
        userName = res.data.userName;
      }
      return res.data;
    } catch (error) {
      history.push(loginPath);
    }
    return undefined;
  };
  // 如果不是登录页面，执行
  const { location } = history;
  if (location.pathname !== loginPath) {
    const currentUser = await fetchUserInfo();
    return {
      currentUser,
    };
  }

  return {};
}

// 添加用户头像显示，用户退出登录
// @ts-ignore
const UserAvatarDropdown = ({ userAvatarUrl: userAvatarUrl, userName }) => {
  const navigate = useNavigate(); // 使用useNavigate钩子进行导航
  const [showUpdatePasswordForm, setShowUpdatePasswordForm] = useState(false); // 添加状态来跟踪表单的显示状态
  const avatarSize = {
    xs: 24,
    sm: 32,
    md: 40,
    lg: 64,
    xl: 80,
    xxl: 100,
  };
  const avatarStyle = {
    borderRadius: '50%', // 可以根据需要调整头像的圆角程度
  };

  const menuItems = [
    {
      label: '个人中心',
      key: 'profile',
      icon: <InfoCircleTwoTone />,
      onClick: () => navigate('/user/info'), // 点击后导航到个人信息页面
    },
    {
      label: '修改密码',
      key: 'editPassword',
      icon: <EditTwoTone />,
      onClick: () => setShowUpdatePasswordForm(true), // 点击时切换表单的显示状态
    },
    {
      label: '退出登录',
      key: 'logout',
      icon:<LogoutOutlined />,
      // 在这里处理退出登录的逻辑
      onClick: async () => {
        // 在这里处理退出登录的逻辑
        // 清除用户信息 等操作
        localStorage.removeItem('currentUser');
        localStorage.removeItem('token');
        localStorage.removeItem('userName');
        localStorage.removeItem('userAvatarUrl');
        // 调用后端的退出登录接口
        await userLogoutUsingPost();
        // 然后跳转到登录页面
        history.push(loginPath);
      },
    },

  ];
  return (
    <>
      <Dropdown menu={{ items: menuItems }}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <Avatar
            src={userAvatarUrl}
            size={userAvatarUrl ? avatarSize.md : avatarSize.sm}
            style={{ ...avatarStyle, backgroundColor: userAvatarUrl ? 'transparent' : '' }}
          />
          {/* 在头像下方显示用户名（如果空间足够的话） */}
          <div style={{ marginLeft: '8px', fontWeight: 'bold' }}>{userName}</div>
        </div>
      </Dropdown>
      {showUpdatePasswordForm && <UpdatePasswordForm
        updateModalOpen={showUpdatePasswordForm}
        onCancel={() => setShowUpdatePasswordForm(false)}
        onSubmit={()=>{}} // 插入submit属性
      />
      }
    </>
  );

};


// ProLayout 支持的api https://procomponents.ant.design/components/layout
export const layout: RunTimeLayoutConfig = ({initialState}) => {
  return {
    logo: 'https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/logo/logo_BI.svg',
    title: "智汇数据平台",
    actionsRender: () => [ <UserAvatarDropdown userAvatarUrl={userAvatarUrl} userName={userName} key="avatar"/>],
    // 获取用户登录信息：
    avatarProps: {
      src: initialState?.currentUser?.userAvatar,
      // title: <AvatarName/>,
      render: (_, avatarChildren) => {
        // return <AvatarDropdown>{avatarChildren}</AvatarDropdown>;
      },
    },
    waterMarkProps: {
      content: initialState?.currentUser?.userName,
    },
    footerRender: () => <Footer />,
    onPageChange: () => {
      const { location } = history;
      // 如果没有登录，重定向到 login
      if (!initialState?.currentUser && location.pathname !== loginPath) {
        history.push(loginPath);
      }
    },
    layoutBgImgList: [
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/D2LWSqNny4sAAAAAAAAAAAAAFl94AQBr',
        left: 85,
        bottom: 100,
        height: '303px',
      },
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/C2TWRpJpiC0AAAAAAAAAAAAAFl94AQBr',
        bottom: -68,
        right: -45,
        height: '303px',
      },
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/F6vSTbj8KpYAAAAAAAAAAAAAFl94AQBr',
        bottom: 0,
        left: 0,
        width: '331px',
      },
    ],
    links: isDev
      ? [
        <Link key="openapi" to="/umi/plugin/openapi" target="_blank">
          <LinkOutlined />
          <span>OpenAPI 文档</span>
        </Link>,
      ]
      : [],

    menuHeaderRender: undefined,
    // 自定义 403 页面
    unAccessible: NoAuth,
    // 增加一个 loading 的状态
    childrenRender: (children) => {
      // if (initialState?.loading) return <PageLoading />;
      return (
        <>
          {children}
          {/*<UserAvatar /> /!* 添加用户头像展示组件 *!/*/}

        </>
      );
    },
  };
};

/**
 * @name request 配置，可以配置错误处理
 * 它基于 axios 和 ahooks 的 useRequest 提供了一套统一的网络请求和错误处理方案。
 * @doc https://umijs.org/docs/max/request#配置
 */
export const request = {
  baseURL: isDev ? 'http://localhost:8088' : 'http://localhost:8088',
  withCredentials: true,
  ...errorConfig,
};
