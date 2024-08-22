import React, {useEffect, useState} from 'react';
import { EditOutlined, LoadingOutlined, PlusOutlined } from '@ant-design/icons';
import {Avatar, Card, Modal, Button, Form, Input, Select, message, Upload, GetProp, UploadProps} from 'antd';
import {getLoginUserUsingGet, updateMyUserUsingPost} from "@/services/wisdomBI/userController";
import {uploadUsingPost} from "@/services/wisdomBI/fileController";
import {getInitialState} from "@/app";
import * as https from "https";
const {Meta} = Card;

type FileType = Parameters<GetProp<UploadProps, 'beforeUpload'>>[0];
const beforeUpload = (file: FileType) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    message.error('只能上传 JPG/PNG 图像文件!');
  }
  const isLt2M = file.size / 1024 / 1024 < 1;
  if (!isLt2M) {
    message.error('图像必须小于 1MB!');
  }
  return isJpgOrPng && isLt2M;
};
// @ts-ignore
const EditUserInfoModal = ({open, onCancel, onFinish, user}) => {
  const [form] = Form.useForm();
  const [userAvatar, setUserAvatar] = useState(); // 初始头像设置为用户已有的头像URL
  const [loading, setLoading] = useState(false); // 上传加载状态
  // console.log(user);
  useEffect(() => {
    if (user) {
      form.setFieldsValue(user);
      setUserAvatar(user.userAvatar);
    }
  }, [user]); // 确保仅在 `user` 更新时触发
  const handleUploadChange = async (file: string | Blob) => {
    setLoading(true);
    const formData = new FormData();
    formData.append('file', file);
    const  response = await  uploadUsingPost({
      file: file,
    });

    if (response && response.code === 0) { // 假设响应对象中的code为0表示上传成功
      console.log('头像上传成功');
      setLoading(false);
      // @ts-ignore
      setUserAvatar(response.data); // 设置头像URL到状态中
      user.userAvatar = response.data;
      console.log("头像URL: "+ response.data);
    } else {
      console.log('头像上传失败');
    }
  };
  const handleOk = () => {
    form.validateFields().then(values => {
      onFinish(values);
      onCancel();
    }).catch(info => {
      console.log('Validate Failed:', info);
    });
  };

  const uploadButton = (
    <button style={{ border: 0, background: 'none' }} type="button">
      {loading ? <LoadingOutlined /> : <PlusOutlined />}
      <div style={{ marginTop: 8 }}>Upload</div>
    </button>
  );
  return (
    <Modal
      title="编辑用户信息"
      open={open}
      onCancel={onCancel}
      footer={[
        <Button key="back" onClick={onCancel}>取消</Button>,
        <Button key="submit" type="primary" onClick={handleOk}>确认</Button>,
      ]}
    >
      <Form form={form} layout="vertical" initialValues={user}>
        <Form.Item name="userAvatar"> {/* 这里添加头像上传组件 */}
          <Upload
            name="userAvatar"
            listType="picture-card"
            className="avatar-uploader"
            showUploadList={false}
            action="https://660d2bd96ddfa2943b33731c.mockapi.io/api/upload"
            beforeUpload={async (file) => {
              beforeUpload(file)
              await handleUploadChange(file); // 自定义上传处理
              return false; // 阻止默认上传
            }}
            accept="image/*"
            maxCount={1}
          >
            { userAvatar ? <img src={userAvatar} alt="avatar" style={{ width: '100%' }} /> : uploadButton}
          </Upload>
        </Form.Item>
        <Form.Item name="userName" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
          <Input placeholder="请输入用户名" />
        </Form.Item>
        <Form.Item
          name="userGender"
          label="性别"
          rules={[{ required: true, message: '请选择性别' }]}
        >
          <Select placeholder="请选择性别">
            <Select.Option value={0}>女</Select.Option>
            <Select.Option value={1}>男</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item name="userProfile" label="个人简介">
          <Input placeholder="请输入个人简介" />
        </Form.Item>
      </Form>
    </Modal>
  );
};

const UserInfo = () => {
  const [user, setUserInfo] = useState<API.LoginUserVO>({});
  const [editOpen, setEditOpen] = useState(false);

  const handleEdit = () => setEditOpen(true);
  const handleCancel = () => setEditOpen(false);
  useEffect(() => {
    getLoginUserUsingGet().then((response) => {
      if (response.code === 0) {
        // @ts-ignore
        setUserInfo(response.data);
        // console.log(user);
      }
    });
  }, []);
  const handleFinish = (values: any) => {
    // @ts-ignore
    values.userAvatar = user.userAvatar;
    updateMyUserUsingPost(values).then((response) => {
      if (response.code === 0) {
        message.success('更新成功');
        // @ts-ignore
        setUserInfo({ ...user, ...values });
        setEditOpen(false);
        // 更新用户信息
        getInitialState();
      } else {
        message.error('更新失败');
      }
    });

  };

  // const user = {
  //   userAvatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=8',
  //   userName: '张三',
  //   userProfile: '这是用户的个人简介',
  //   userPhone: '123456789',
  //   userGender: 1,
  //   userAccount: 'zhangsan123',
  // };


  return (
    <div
      style={{
        backgroundImage: `url('https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/logo/bg.png')`,
        backgroundRepeat: 'no-repeat',
        backgroundPosition: 'center',
        backgroundSize: 'cover',
        minHeight: '100vh', // 确保页面填满整个视口
      }}
    >

      <Card
        style={{
          width: 800,
          margin: 'auto',
          display: 'block',
          textAlign: 'center',
          borderRadius: '10px',
          padding: '20px',
          backgroundColor: '#fff',
        }}
        cover={<img
          src={user?.userAvatar}
          alt="Background"
          style={{width: '600px', height: '600px'}}
        />}
        actions={[
          <Button key="edit" type={'default'} icon={<EditOutlined />} onClick={handleEdit}>
            编辑
          </Button>,
        ]}
      >
        <Meta
          avatar={<Avatar src={user?.userAvatar}
                          style={{width: 100,
                                  height: 100,
                                  border: '2px solid #95a5a6',
                                   borderRadius: '50%', }}/>}
          title={user?.userName}
          description={
            <>
              <div>性别: {user?.userGender === 1 ? '男' : '女'}</div>
              <div>账户名: {user?.userAccount}</div>
              <div>手机: {user?.userPhone}</div>
              <div>简介：{user?.userProfile}</div>
            </>
          }
        />
      </Card>
      <EditUserInfoModal
        open={editOpen}
        onCancel={handleCancel}
        onFinish={handleFinish}
        user={user}
      />
    </div>
  );
};

export default UserInfo;
