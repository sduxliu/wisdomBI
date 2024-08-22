import '@umijs/max';
import {Button, Form, Input, Modal, Select} from 'antd';
import React from 'react';

export type FormValueType = {
  userRole?: string;
} & Partial<API.User>;

export type UpdateFormProps = {
  onCancel: (flag?: boolean, formValues?: FormValueType) => void;
  onSubmit: (values: FormValueType) => Promise<void>;
  updateModalOpen: boolean;
  values: Partial<API.User>;
};

const UpdateForm: React.FC<UpdateFormProps> = (props) => {
  return (
    <Modal
      width={500}
      style={{
        padding: '32px 40px 48px',
      }}
      destroyOnClose
      title="修改用户权限"
      visible={props.updateModalOpen}
      onCancel={() => {
        props.onCancel();
      }}
      footer={null} // Custom footer to control the buttons manually
    >
      <Form
        initialValues={{
          userRole: props.values.userRole,
         id: props.values.id,
        }}
        onFinish={props.onSubmit} // Use onFinish instead of onSubmit
      >
        <Form.Item label={'用户ID'} name={'id'}>
          <Input disabled={true} />
        </Form.Item>
        <Form.Item label="用户权限" name="userRole" style={{ width: '50%' }}>
          <Select placeholder="选择用户权限">
            <Select.Option value="admin">管理员</Select.Option>
            <Select.Option value="user">普通用户</Select.Option>
            <Select.Option value="banned">封禁</Select.Option>
          </Select>
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit">
            提交
          </Button>
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default UpdateForm;
