import { CHART_STATUS } from '@/constants/chart/chartStatus';
import { CHART_TYPE_JSON } from '@/constants/chart/chartType';
import CreateChartModal from '@/pages/Admin/Chart/components/CreateChartModal';
import UpdateChartModal from '@/pages/Admin/Chart/components/UpdateChartModal';
import { deleteChartUsingPost, listChartByPageUsingPost } from '@/services/wisdomBI/chartController';
import {
  ActionType,
  FooterToolbar,
  PageContainer,
  ProColumns,
  ProTable,
} from '@ant-design/pro-components';
import {Button, Divider, message, Popconfirm, Space, Tooltip, Typography} from 'antd';
import React, { useRef, useState } from 'react';

/**
 * 图表管理页面
 * @constructor
 */
const AdminChartPage: React.FC<unknown> = () => {
  const [createModalVisible, setCreateModalVisible] = useState<boolean>(false);
  const [selectedRowsState, setSelectedRows] = useState<API.Chart[]>([]);
  const [updateModalVisible, setUpdateModalVisible] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);
  const [updateData, setUpdateData] = useState<API.Chart>({});
  const actionRef = useRef<ActionType>();

  /**
   *  删除节点
   * @param selectedRows
   */
  const doDelete = async (selectedRows: API.Chart[]) => {
    const hide = message.loading('正在删除');
    if (!selectedRows) return true;
    try {
      await deleteChartUsingPost({
        id: selectedRows.find((row) => row.id)?.id || 0,
      });
      message.success('操作成功');
      actionRef.current?.reload();
    } catch (e: any) {
      message.error('操作失败，' + e.message);
    } finally {
      hide();
    }
  };

  /**
   * 表格列配置
   */
  const columns: ProColumns<API.Chart>[] = [
    {
      title: 'id',
      dataIndex: 'id',
      valueType: 'index',
      width: 50,
    },
    {
      title: '图表名称',
      dataIndex: 'name',
      valueType: 'text',
      ellipsis: true, // Enable ellipsis
      width: 120, // Set a fixed width
    },
    {
      title: '图表类型',
      dataIndex: 'chartType',
      valueType: 'text',
      valueEnum: CHART_TYPE_JSON,
      ellipsis: true,
      width: 120,
    },
    {
      title: '原始数据',
      dataIndex: 'chartData',
      valueType: 'text',
      ellipsis: true,
      width: 120,
      render: (text) => (
        <Tooltip title={text}>
          {text}
        </Tooltip>
      ), // Tooltip for full text on hover
    },
    {
      title: '分析目标',
      dataIndex: 'goal',
      valueType: 'textarea',
      ellipsis: true,
      width: 120,
      render: (text) => (
        <Tooltip title={text}>
          {text}
        </Tooltip>
      ),
    },
    {
      title: 'AI生成图表数据',
      dataIndex: 'genChart',
      valueType: 'text',
      ellipsis: true,
      width: 120,
      render: (text) => (
        <Tooltip title={text}>
          {text}
        </Tooltip>
      ),
    },
    {
      title: 'AI分析结论',
      dataIndex: 'genResult',
      valueType: 'textarea',
      ellipsis: true,
      width: 120,
      render: (text) => (
        <Tooltip title={text}>
          {text}
        </Tooltip>
      ),
    },
    {
      title: '生成状态',
      dataIndex: 'chartStatus',
      valueType: 'text',
      valueEnum: CHART_STATUS,
      ellipsis: true,
      width: 100,
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      valueType: 'dateTime',
      hideInForm: true,
      width: 100,
    },
    {
      title: '操作',
      dataIndex: 'option',
      valueType: 'option',
      render: (_, record) => (
        <Space split={<Divider type="vertical" />}>
          <Typography.Link
            onClick={() => {
              setUpdateData(record);
              setUpdateModalVisible(true);
            }}
          >
            修改
          </Typography.Link>
          <Popconfirm
            title="您确定要删除么？"
            onConfirm={() => doDelete([record])}
            okText="确认"
            cancelText="取消"
          >
            <Typography.Link type="danger">删除</Typography.Link>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <PageContainer>
      <ProTable<API.Chart>
        headerTitle="图表管理"
        actionRef={actionRef}
        loading={loading}
        rowKey="id"
        search={{
          labelWidth: 'auto',
        }}
        toolBarRender={() => [
          <Button key="1" type="primary" onClick={() => setCreateModalVisible(true)}>
            新建
          </Button>,
        ]}
        request={async (params, sorter) => {
          setLoading(true);
          const searchParams: API.ChartQueryRequest = {
            ...params,
            sortField: 'createTime',
            sortOrder: 'desc',
          };
          console.log(searchParams);
          const { data, code } = await listChartByPageUsingPost(searchParams);
          setLoading(false);
          return {
            data: data?.records || [],
            success: code === 0,
            total: data?.total,
          } as any;
        }}
        columns={columns}
        // 选择多行
        rowSelection={{
          onChange: (_, selectedRows) => {
            setSelectedRows(selectedRows);
          },
        }}
      />
      {selectedRowsState.length > 0 && (
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
              项
            </div>
          }
        >
          <Popconfirm
            title="确认删除所选项吗？"
            onConfirm={() => doDelete(selectedRowsState)}
            okText="确认"
            cancelText="取消"
          >
            <Button type="dashed" size="large" danger>
              批量删除
            </Button>
          </Popconfirm>
        </FooterToolbar>
      )}

      <CreateChartModal
        modalVisible={createModalVisible}
        columns={columns}
        onSubmit={() => setCreateModalVisible(false)}
        onCancel={() => setCreateModalVisible(false)}
      />
      <UpdateChartModal
        oldData={updateData}
        modalVisible={updateModalVisible}
        columns={columns}
        onSubmit={() => setUpdateModalVisible(false)}
        onCancel={() => setUpdateModalVisible(false)}
      />
    </PageContainer>
  );
};

export default AdminChartPage;
