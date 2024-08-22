// eslint-disable-next-line @typescript-eslint/no-unused-vars
import {LockOutlined, MailOutlined, MobileOutlined, UserOutlined,} from '@ant-design/icons';
import {Button, Form, message, Modal, Tabs} from 'antd';
import React, {useState} from 'react';
import {history} from 'umi';
import Footer from '@/components/Footer';
import {sendCaptchaUsingPost, userRegisterUsingPost} from '@/services/wisdomBI/userController';
import {LoginForm, ProFormText} from '@ant-design/pro-form';
import queryString from "query-string";
import {Helmet} from "@@/exports";
import Settings from "../../../../config/defaultSettings";

const Register: React.FC = () => {
  const [type, setType] = useState<string>('account');
  // 发送验证码
  const [isDisabled, setIsDisabled] = useState(false);
  const [isSendingCaptcha, setIsSendingCaptcha] = useState(false);
  const [countdown, setCountdown] = useState(60);
  const [form] = Form.useForm();

  const sendCaptcha = async () => {
    // 获取手机号
    if (isDisabled || isSendingCaptcha) return;
    const phone = form.getFieldValue("userPhone"); // 直接从表单中获取手机号
    console.log(phone);
    if(!phone)
      return Modal.error({
        title: '手机号不能为空',
      });
    if(!/^1[3456789]\d{9}$/.test(phone))
      return Modal.error({
        title: '手机号格式错误',
      });
    setIsSendingCaptcha(true);
    setIsDisabled(true);
    setCountdown(60);

    try {
      await sendCaptchaUsingPost({userPhone: phone}); // 发送验证码
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

  // 表单提交
  const handleSubmit = async (values: API.UserRegisterRequest) => {
    const {userPassword, checkPassword} = values;
    // 校验
    if (userPassword !== checkPassword) {
      message.error('两次输入的密码不一致');
      return;
    }

      // 注册
      const res = await userRegisterUsingPost(values);
      if (res.code === 0) {
        const defaultLoginSuccessMessage = '注册成功！';
        message.success(defaultLoginSuccessMessage);
        // 跳转到登录页——不保存历史记录
        history.push('/user/login');
        return;
      }else{
        message.error(res.message);
      }

  };

  const checkPasswordsMatch = async (_: any, value: any) => {
    if (value !== form.getFieldValue('userPassword')) {
      throw new Error('两次输入的密码不一致!');
    }
  };

  return (
    <div >
      <Helmet>
        <title>
          {'注册'}- {Settings.title}
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
          submitter={{
            searchConfig: {
              submitText: '注册'
            }
          }}
          logo={<img alt="logo" src="/logo.svg"/>}
          title="智汇数据平台"
          subTitle="智能分析数据，调用开放接口，汇聚智慧"
          initialValues={{
            autoLogin: true,
          }}
          onFinish={async (values) => {
            await handleSubmit(values as API.UserRegisterRequest);
          }}
        >
          <div style={{display: 'flex', justifyContent: 'center'}}>
            <Tabs activeKey={type} onChange={setType}>
              <Tabs.TabPane key="account" tab="账号密码注册"/>
            </Tabs>
          </div>
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
                  {
                    min: 8,
                    type: 'string',
                    message: '长度不能小于 8',
                  },
                ]}
              />
              <ProFormText.Password
                name="checkPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined/>,
                }}
                placeholder="请确认密码"
                rules={[
                  {
                    required: true,
                    message: '确认密码是必填项！',
                  },
                  {validator: checkPasswordsMatch},
                ]}
              />
              <ProFormText
                name="userPhone"
                fieldProps={{
                  size: 'large',
                  prefix: <MobileOutlined/>,
                }}
                placeholder="请输入手机号"
                rules={[
                  {
                    required: true,
                    message: '手机号是必填项！',
                  },
                  {
                    // pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, // 自定义邮箱格式验证
                    pattern: /^1\d{10}$/,
                    message: '不合法的手机号！',
                    // message: '请输入有效的邮箱地址！',
                  },
                ]}
              />
              <div style={{display: 'flex', alignItems: 'center'}}>
                <ProFormText
                  name="captcha"
                  placeholder={'请输入验证码！'}
                  style={{flex: 1}}
                  rules={[{required: true, message: '请输入验证码！'}]}
                />
                <Button type="primary" onClick={sendCaptcha} disabled={isDisabled}
                        style={{width: '100px', marginBottom: '20px', marginLeft: '10px'}}>
                  {isDisabled ? `${countdown}秒后重发` : '获取验证码'}
                </Button>
              </div>
            </>
          )}
        </LoginForm>
      </div>
      <Footer/>
    </div>
  );
};

export default Register;
