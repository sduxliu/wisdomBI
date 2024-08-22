import React, {useEffect, useState} from 'react';
import { ProFormText, StepsForm } from '@ant-design/pro-form';
import {Modal, Button, Form, message} from 'antd';
import {ProForm} from "@ant-design/pro-form/lib";
import {
  forgetPasswordUsingPost,
  sendCaptchaUsingPost,
  updatePasswordUsingPost
} from "@/services/wisdomBI/userController";
import {r} from "@umijs/utils/compiled/tar";
import {values} from "lodash";


const UpdatePasswordForm = (props: { updateModalOpen: boolean | undefined; onCancel: () => void; onSubmit: ((values: Record<string, any>) => Promise<boolean | void>) | undefined; }) => {
  const [isDisabled, setIsDisabled] = useState(false);
  const [isSendingCaptcha, setIsSendingCaptcha] = useState(false);
  const [countdown, setCountdown] = useState(60);
  // const [form] = ProForm.useForm();
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

  const checkPasswordsMatch = async (_: any, value: any) => {
    if (value !== form.getFieldValue('userPassword')) {
      throw new Error('两次输入的密码不一致!');
    }
  };
  const handlePasswordUpdate = async (values: Record<string, any>) => { // values: Record<string, any> 或submit的回调后发送的参数
    console.log("更新密码："+values);

    if (!values.userPhone || !values.userPassword || !values.checkPassword || !values.captcha) {
      message.error('请输入完整信息');
      return;
    }
    if (JSON.stringify(values) === '{}') {
      console.log("Values is empty");
      message.error('请输入完整信息');
      return;
    }
    try {
      // 调用后端的修改密码接口
      const response = await forgetPasswordUsingPost(values);

      if (response.code === 0) {
        // 关闭模态框
        props.onCancel();
        alert('密码修改成功');
      } else {

        alert('密码更新失败: ' + response.message);
      }
    } catch (error) {
      console.error('更新密码时出错:', error);
      alert('发生错误，请重试');
    }
  };
  return (
    <Form form={form} layout="vertical">
      <Modal
        width={500}
        bodyStyle={{ padding: '32px 40px 48px' }}
        destroyOnClose
        title="修改密码"
        open={props.updateModalOpen}
        footer={null}
        onCancel={() => props.onCancel()}
      >
        <Form.Item>
          <ProFormText
            name="userPhone"
            placeholder={'请输入手机号！'}
            width="md"
            rules={[
              {required: true, message: '请输入手机号！'},
              {pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号！'},
            ]}
            // onChange={(e) => console.log('当前输入的手机号:', e.target.value)}
          />
          <ProFormText.Password
            name="userPassword"
            placeholder={'请输入新密码！'}
            width="md"
            rules={[
              {required: true, message: '请输入新密码！'},
              {
                min: 8,
                type: 'string',
                message: '长度不能小于 8',
              },
            ]}
          />
          <ProFormText.Password
            name="checkPassword"
            placeholder={'请确认密码！'}
            width="md"
            rules={[
              {required: true, message: '请确认密码！'},
              {validator: checkPasswordsMatch},
            ]}
          />
          <div style={{display: 'flex', alignItems: 'center'}}>
            <ProFormText
              name="captcha"
              placeholder={'请输入验证码！'}
              style={{flex: 1}}
              rules={[{required: true, message: '请输入验证码！'}]}
            />
            <Button type="primary" onClick={sendCaptcha} disabled={isDisabled} style={{width:'100px', marginBottom:'20px', marginLeft: '10px'}}>
              {isDisabled ? `${countdown}秒后重发` : '获取验证码'}
            </Button>
          </div>
          {/*提交按钮需要判断数据是否为空：*/}
          <Button type="primary"  htmlType={'submit'} onClick={() => handlePasswordUpdate(form.getFieldsValue())} style={{marginTop: '16px'}}>
            提交更改
          </Button>
        </Form.Item>
      </Modal>
    </Form>
  );
};

export default UpdatePasswordForm;

