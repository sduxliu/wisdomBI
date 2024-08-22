import {LoadingOutlined, PlusOutlined} from '@ant-design/icons';
import {ActionType, ProColumns, ProDescriptionsItemProps, ProFormSelect} from '@ant-design/pro-components';
import {
  FooterToolbar,
  ModalForm,
  PageContainer,
  ProDescriptions,
  ProFormText,
  ProTable,
} from '@ant-design/pro-components';
import '@umijs/max';
import {Button, Drawer, GetProp, message, PopconfirmProps, Upload, UploadProps} from 'antd';
import React, {useRef, useState} from 'react';
import type { FormValueType } from '@/pages/Admin/Users/components/UpdateForm';
import UpdateForm from '@/pages/Admin/Users/components/UpdateForm';
import {
  addUserUsingPost, deleteUserBatchUsingPost,
  deleteUserUsingPost, listUserByPageUsingPost, updateUserUsingPost
} from '@/services/wisdomBI/userController';
import {ProForm} from "@ant-design/pro-form/lib";
import {uploadUsingPost} from "@/services/wisdomBI/fileController";
import Popconfirm from 'antd/lib/popconfirm';
import {ids} from "@umijs/bundler-webpack/compiled/webpack";



/**
 * @en-US Add node
 * @zh-CN 添加节点
 * @param fields
 */
const handleAdd = async (fields: API.UserAddRequest) => {
  const hide = message.loading('正在添加');
  try {
    await addUserUsingPost({
      ...fields,
    });
    hide();
    message.success('添加成功');
    return true;
  } catch (error) {
    hide();
    message.error('添加失败，请重试');
    return false;
  }
};

/**
 * @en-US Update node
 * @zh-CN 更新节点
 *
 * @param fields
 */
const handleUpdate = async (fields: FormValueType) => {
  const hide = message.loading('正在更新');
  console.log("更新："+fields.userRole);
  console.log("更新："+fields.id);
  try {
    await updateUserUsingPost({
      id: fields.id,
      userRole: fields.userRole

    });
    hide();
    message.success('更新成功');
    return true;
  } catch (error) {
    hide();
    message.error('信息更新失败，请重试');
    return false;
  }
};

/**
 *  Delete node
 * @zh-CN 删除节点
 *
 * @param selectedRows
 */

const handleRemove = async (selectedRows: API.DeleteRequest[]) => {
  const hide = message.loading('正在删除');
  if (!selectedRows) return true;
  // 如果只选择了一个
  if (selectedRows.length === 1) {

      // 如果只选择了一个
      const row = selectedRows[0];
      // 删除单个
      const res = await deleteUserUsingPost({
        id: row.id,
      });
      hide();
      if(res.code === 0){
        message.success('删除成功');
        return true;
      }else{
        message.error('删除失败，请重试' + res.message);
      }

    }
  else{
    await  deleteUserBatchUsingPost(
      {
        ids: selectedRows
          .filter(row => row.id !== undefined) // 过滤掉 id 为 undefined 的项
          .map((row) => row.id as number) // 明确地将 id 断言为 number 类型
      }
    );
    hide();
    message.success('删除成功');
    return true;
  }
};


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
const TableList: React.FC = () => {
  /**
   * @en-US Pop-up window of new window
   * @zh-CN 新建窗口的弹窗
   *  */
  const [createModalOpen, handleModalOpen] = useState<boolean>(false);
  /**
   * @en-US The pop-up window of the distribution update window
   * @zh-CN 分布更新窗口的弹窗
   * */
  const [updateModalOpen, handleUpdateModalOpen] = useState<boolean>(false);
  const [showDetail, setShowDetail] = useState<boolean>(false);
  const actionRef = useRef<ActionType>();
  const [currentRow, setCurrentRow] = useState<API.User>();
  const [selectedRowsState, setSelectedRows] = useState<API.User[]>([]);
  // 上传状态
  const [loading, setLoading] = useState(false);
// 保存头像上传状态
  const [userAvatar, setUserAvatar] = useState('');
  const handleUploadChange = async (file: string | Blob) => {
    setLoading(true);
    const formData = new FormData();
    formData.append('file', file);

    // const response = await fetch('/upload/img', {
    //   method: 'POST',
    //   body: formData,
    // });
    const  response = await  uploadUsingPost({
      file: file,
    });

    if (response && response.code === 0) { // 假设响应对象中的code为0表示上传成功
      console.log('头像上传成功');
      setLoading(false);
      // @ts-ignore
      setUserAvatar(response.data); // 设置头像URL到状态中
      console.log("头像URL: "+ response.data);
    } else {
      console.log('头像上传失败');
    }
  };
  const uploadButton = (
    <button style={{ border: 0, background: 'none' }} type="button">
      {loading ? <LoadingOutlined /> : <PlusOutlined />}
      <div style={{ marginTop: 8 }}>Upload</div>
    </button>
  );


  const confirm: PopconfirmProps['onConfirm'] = async () => {
    await handleRemove(selectedRowsState);
    setSelectedRows([]);
    actionRef.current?.reloadAndRest?.();
  };
  /**
   * @en-US International configuration
   * @zh-CN 国际化配置
   * */

    // 渲染用户详情模态框
// 表格列定义
  const columns: ProColumns<API.User>[] = [
    {
      title: '用户ID',
      dataIndex: 'id',
      valueType: 'index',
      render: (dom, entity) => {
        return (
          <a
            onClick={() => {
              setCurrentRow(entity);
              setShowDetail(true);
            }}
          >
            {dom}
          </a>
        );
      },
    },
    {
      title: '用户账户',
      dataIndex: 'userAccount',
      valueType: 'text',
    },
    {
      title: '用户电话',
      dataIndex: 'userPhone',
      valueType: 'text',
    },
    {
      title: '用户性别',
      dataIndex: 'userGender',
      valueEnum: {
        0: { text: '女', status: 'Default' },
        1: { text: '男', status: 'Processing' },
      },
      hideInForm: true,
    },
    {
      title: '用户角色',
      dataIndex: 'userRole',
      valueEnum: {
        admin: { text: '管理员', status: 'Success' },
        user: { text: '普通用户', status: 'Processing' },
        banned: { text: '封禁用户', status: 'Error' },
      },
      hideInForm: true,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      valueType: 'dateTime',
      hideInForm: true,
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      valueType: 'dateTime',
      hideInForm: true,
    },
      {
        title: '操作',
        dataIndex: 'option',
        valueType: 'option',
        render: (_, record) => [
          <a
            key="config"
            onClick={() => {
              handleUpdateModalOpen(true);
              setCurrentRow(record);
            }}
          >
            权限
          </a>,
          <Popconfirm
            key="delete"
            title="确认删除该用户吗？"
            onConfirm={() => {
              handleRemove([record]);
              actionRef.current?.reloadAndRest?.();
            }}
            okText="确认"
            cancelText="取消"
          >
            <a key="delete">删除</a>
          </Popconfirm>,

        ],
      },
  ];


  return (
    <PageContainer>
      <ProTable<API.User, API.ChartQueryRequest>
        headerTitle={'查询表格'}
        actionRef={actionRef}
        rowKey="id"
        search={{
          labelWidth: 120,
        }}
        toolBarRender={() => [
          <Button
            type="primary"
            key="primary"
            onClick={() => {
              handleModalOpen(true);
              setCurrentRow(undefined);

            }}
          >
            <PlusOutlined /> 新建
          </Button>,
        ]}
        request={async (params, sorter) => {
          setLoading(true);
          const searchParams: API.UserQueryRequest = {
            ...params,
            sortField: 'createTime',
            sortOrder: sorter.order === 'descend' ? 'desc' : 'asc',
          };
          const { data, code } = await listUserByPageUsingPost(searchParams);
          setLoading(false);
          return {
            data: data?.records || [],
            success: code === 0,
            total: data?.total,
          } as any;
        }}

        columns={columns}
        rowSelection={{
          onChange: (_, selectedRows) => {
            setSelectedRows(selectedRows);
          },
          // @ts-ignore
          selectedRowKeys: selectedRowsState.map(row => row.id), // 保持选择的行同步
          preserveSelectedRowKeys: true, // 数据更改时保持选择
        }}
      />
      {selectedRowsState?.length > 0 && (
        <FooterToolbar
          extra={
            <div>
              已选择{' '}
              <a
                style={{
                  fontWeight: 600,
                }}
              >
                {selectedRowsState.length}
              </a>{' '}
              项 &nbsp;&nbsp;
            </div>
          }
        >
          <div>
            {/* 其他UI组件 */}
            <Popconfirm
              title="确认删除所选项吗？"
              description="您确定要删除选中的项吗？"
              onConfirm={confirm} // 点击确认按钮时的回调函数
              onCancel={() => console.log('取消了删除操作')} // 点击取消按钮时的回调函数（可以自定义为更复杂的逻辑）
              okText="确认"
              cancelText="取消"
            >
              <Button type="dashed" size="large">批量删除</Button>
            </Popconfirm>
          </div>
        </FooterToolbar>
      )}
      <ModalForm
        title={'添加用户'}
        width="400px"
        open={createModalOpen}
        onOpenChange={handleModalOpen}
        onFinish={async (value) => {
          // 在这里添加用户头像的URL到values中
          value.userAvatar = userAvatar;
          const success = await handleAdd(value as API.User);
          if (success) {
            // 清空表单数据 TODO 待优化
            setUserAvatar(''); // 清空用户头像数据
            handleModalOpen(false); // 关闭模态框
            if (actionRef.current) {
              actionRef.current.reload();
            }
          }
        }}
      >
          <ProForm.Item
            label="上传头像"
            name="userAvatar"
            initialValue={""}
            valuePropName="fileList"
            getValueFromEvent={({ fileList }) => fileList}
          >
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
              {userAvatar ? <img src={userAvatar} alt="avatar" style={{ width: '100%' }} /> : uploadButton}
            </Upload>
          </ProForm.Item>
        <ProFormText
          label="用户账户"
          rules={[
            {
              required: true,
              message: '用户账户名为必填项',
            },
          ]}
          width="md"
          placeholder={'请输入用户账户名'}
          name="userAccount"
          initialValue={""}
        />
        <ProFormText
          label="用户昵称"
          width="md"
          placeholder={'请输入用户昵称'}
          name="userName"
          initialValue={""}
        />
        <ProFormSelect
          label="用户权限"
          width="md"
          placeholder="选择用户权限"
          name="userRole"
          options={[
            { label: '管理员', value: 'admin' },
            { label: '普通用户', value: 'user' },
          ]}
        />
      </ModalForm>
      <UpdateForm
        onSubmit={async (value) => {
          const success = await handleUpdate(value);
          if (success) {
            handleUpdateModalOpen(false);
            setCurrentRow(undefined);
            if (actionRef.current) {
              actionRef.current.reload();
            }
          }
        }}
        onCancel={() => {
          handleUpdateModalOpen(false);
          if (!showDetail) {
            setCurrentRow(undefined);
          }
        }}
        updateModalOpen={updateModalOpen}
        values={currentRow || {}}
      />
    </PageContainer>
  );
};
export default TableList;
