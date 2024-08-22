import Footer from '@/components/Footer';
import {
  getLoginUserUsingGet, sendCaptchaUsingPost, updatePasswordUsingPost, userLoginByPhoneUsingPost,
  userLoginUsingPost,
} from '@/services/wisdomBI/userController';
import { Link } from '@@/exports';
import { LockOutlined, UserOutlined,MobileOutlined  } from '@ant-design/icons';
import {
  LoginForm,
  ProFormText,
} from '@ant-design/pro-components';
import { useEmotionCss } from '@ant-design/use-emotion-css';
import { Helmet, history, useModel } from '@umijs/max';
import {Alert, Button, Form, message, Modal, Tabs} from 'antd';
import React, {useState} from 'react';
import { flushSync } from 'react-dom';
import Settings from '../../../../config/defaultSettings';
import UpdatePasswordForm from "@/pages/User/EditPassword";

const Login: React.FC = () => {
  const [userLoginState, setUserLoginState] = useState<API.LoginResult>({});
  const [type, setType] = useState<string>('account');
  const {setInitialState } = useModel('@@initialState');
  const [showUpdatePasswordForm, setShowUpdatePasswordForm] = useState(false); // 添加状态来跟踪表单的显示状态

  const containerClassName = useEmotionCss(() => {
    return {
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      overflow: 'auto',
      backgroundImage:
        "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
      backgroundSize: '100% 100%',
    };
  });
  const LoginMessage: React.FC<{
    content: string;
  }> = ({ content }) => {
    return (
      <Alert
        style={{
          marginBottom: 24,
        }}
        message={content}
        type="error"
        showIcon
      />
    );
  };
  /**
   * 登陆成功后，获取用户登录信息
   */
  const fetchUserInfo = async () => {
    const userInfo = await getLoginUserUsingGet();
    // const userInfo = await initialState?.fetchUserInfo?.();
    if (userInfo) {
      flushSync(() => {
        // @ts-ignore
        setInitialState((s) => ({
          ...s,
          currentUser: userInfo,
        }));
      });
    }
  };

  const handleSubmit = async (values: API.UserLoginRequest,values2: API.UserLoginByPhoneRequest) => {
    try {
      // 登录
      // 1. 账号密码登录
      let res;
      if(type === 'account')
      {
        res = await userLoginUsingPost(values);

      }else if(type === 'mobile'){
        res = await userLoginByPhoneUsingPost(values2);
      }
      // @ts-ignore
      if (res.code === 0) {
        const defaultLoginSuccessMessage = '登录成功！';
        message.success(defaultLoginSuccessMessage);
        await fetchUserInfo();
        const urlParams = new URL(window.location.href).searchParams;
        history.push(urlParams.get('redirect') || '/');
        return;
      } else {
        // 如果失败去设置用户错误信息
        // @ts-ignore
        // setUserLoginState(res.message);
        message.error(res.message);
        // @ts-ignore
        console.log(res.message);
        // 如果失败去设置用户错误信息
        // @ts-ignore
        setUserLoginState(res.message);
      }
    } catch (error) {
      const defaultLoginFailureMessage = '登录失败，请重试！';
      console.log(error);
      message.error(defaultLoginFailureMessage);
    }
  };

  const [isDisabled, setIsDisabled] = useState(false);
  const [isSendingCaptcha, setIsSendingCaptcha] = useState(false);
  const [countdown, setCountdown] = useState(60);
  // const [form] = ProForm.useForm();
  const [form] = Form.useForm();
  const { Item } = Form; // 从Form组件中解构出Item组件

  const sendCaptcha = async () => {
    if (isDisabled || isSendingCaptcha) return;
    const userPhone = form.getFieldValue('userPhone');
    console.log(userPhone);
    // console.log(form.getFieldsValue());
    if(!form.getFieldValue('userPhone'))
      return Modal.error({
        title: '手机号不能为空',
      });
    if(!/^1[3456789]\d{9}$/.test(form.getFieldValue('userPhone')))
      return Modal.error({
        title: '手机号格式错误',
      });
    setIsSendingCaptcha(true);
    setIsDisabled(true);
    setCountdown(60);
    try {
      // const bodyData: API.UserSendCaptchaRequest = { userPhone: userPhone }; // 创建包含手机号的对象作为请求体
      await sendCaptchaUsingPost({userPhone: userPhone}); // 发送验证码
    } catch (error) {
      console.error(error);
    }

    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          setIsSendingCaptcha(false);
          setIsDisabled(false);
          return 60;
        }
        return prev - 1;
      });
    }, 1000);
  };
  const handleOpenForm = () => {
    setShowUpdatePasswordForm(true); // 打开表单
  };
  // 修改密码提交
  const handlePasswordUpdate = async (values: Record<string, any>) => { // values: Record<string, any> 或submit的回调后发送的参数
    console.log("更新密码："+values);
    try {
      // 调用后端的修改密码接口
      const response = await updatePasswordUsingPost(values);

      if (response.code === 0) {
        alert('密码更新成功');
        // 关闭模态框
        setShowUpdatePasswordForm(false);
      } else {
        alert('密码更新失败: ' + response.message);
      }
    } catch (error) {
      console.error('更新密码时出错:', error);
      alert('发生错误，请重试');
    }
  };
  const { status, type: loginType } = userLoginState;
  // @ts-ignore
  return (
    <div className={containerClassName}>
      <Helmet>
        <title>
          {'登录'}- {Settings.title}
        </title>
      </Helmet>
      <div
        style={{
          flex: '1',
          padding: '32px 0',
        }}
      >
        <LoginForm
          form={form}
          contentStyle={{
            minWidth: 280,
            maxWidth: '75vw',
          }}
          logo={<img alt="logo" src="/logo.svg"/>}
          title="智汇数据平台"
          subTitle="智能分析数据，调用开放接口，汇聚智慧"
          onFinish={async (values) => {
            await handleSubmit(values as API.UserLoginRequest, values as API.UserLoginByPhoneRequest);
          }}
        >
          <Tabs
            activeKey={type}
            onChange={setType}
            centered
            items={[
              {
                key: 'account',
                label: '账户密码登录',
              },
              {
                key: 'mobile',
                label: '手机号登录',
              },
            ]}
          />

          {status === 'error' && loginType === 'account' && (
            <LoginMessage content={'错误的用户名和密码'}/>
          )}
          {type === 'account' && (
            <>
              <ProFormText
                name="userAccount"
                fieldProps={{
                  size: 'large',
                  prefix: <UserOutlined/>,
                }}
                placeholder={'请输入用户名'}
                rules={[
                  {
                    required: true,
                    message: '用户名是必填项！',
                  },
                ]}
              />
              <ProFormText.Password
                name="userPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined/>,
                }}
                placeholder={'请输入密码'}
                rules={[
                  {
                    required: true,
                    message: '密码是必填项！',
                  },
                ]}
                style={{marginBottom:'20px'}}
              />
            </>
          )}

          {status === 'error' && loginType === 'mobile' && <LoginMessage content="验证码错误"/>}
          {type === 'mobile' && (
            <>
              <ProFormText
                fieldProps={{
                  size: 'large',
                  prefix: <MobileOutlined/>,
                }}
                name="userPhone"
                placeholder={'请输入手机号！'}
                rules={[
                  {
                    required: true,
                    message: '手机号是必填项！',
                  },
                  {
                    pattern: /^1[3-9]\d{9}$/,
                    message: '不合法的手机号！',
                  },
                ]}
              />
              <Item style={{display: 'flex', justifyContent: 'space-between'}}>
                <div style={{display: 'flex', alignItems: 'center'}}>
                  <ProFormText
                    fieldProps={{
                      size: 'large', // 调整输入框的大小，可以根据实际情况调整，这里使用了medium作为示例
                      prefix: <LockOutlined/>, // 输入框前缀图标，根据实际情况调整或移除
                    }}
                    name="captcha"
                    placeholder="请输入验证码！"
                    rules={[{required: true, message: '请输入验证码！'}]}
                    style={{width: '300px', height: '40px', borderRadius: '5px'}} // 调整宽度、高度以及边框半径等样式，增加美观度
                  />

                  <Button
                    type="primary"
                    onClick={sendCaptcha}
                    disabled={isDisabled}
                    style={{
                      width: '100px',
                      height: '30px',
                      marginLeft: '10px',
                      marginBottom: '20px',
                      borderRadius: '5px'
                    }}
                  >
                    {isDisabled ? `${countdown}秒后重发` : '获取验证码'}
                  </Button>
                </div>
              </Item>
            </>
          )}
          <div
            style={{
              width: '300px',
              marginBottom: 14,
            }}
          >

            <Link to="/user/register" style={{float: 'right'}}>没有账户？立即创建</Link>
            {/*<Link to="/user/forgetPassword" > 找回密码 </Link>*/}
            <Button type="link" onClick={handleOpenForm}>找回密码</Button>
            {showUpdatePasswordForm && (
              <UpdatePasswordForm
                updateModalOpen={showUpdatePasswordForm} // 关联表单的显示状态
                onSubmit={handlePasswordUpdate} // 插入submit属性
                onCancel={() => setShowUpdatePasswordForm(false)} // 关闭表单的函数
              />

            )}
          </div>
        </LoginForm>
      </div>
      <Footer/>
    </div>
  );
};
export default Login;
