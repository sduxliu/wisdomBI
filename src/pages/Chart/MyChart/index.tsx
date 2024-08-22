import {
  deleteChartUsingPost,
  listMyChartByPageUsingPost,
  regenerateChartUsingGet
} from '@/services/wisdomBI/chartController';

import { useModel } from '@@/exports';
import {
  Avatar,
  Button,
  Card,
  Collapse,
  Dropdown,
  Input,
  List,
  MenuProps,
  message,
  Modal,
  Result,
  Select,
  Space
} from 'antd';
import Search from 'antd/es/input/Search';
import ReactECharts from 'echarts-for-react';
import React, {useEffect, useRef, useState} from 'react';
import {
  CheckOutlined,
  DeleteOutlined,
  DownOutlined, EditOutlined,
  EyeOutlined,
  RedoOutlined,
  ReloadOutlined, SmallDashOutlined
} from "@ant-design/icons";
import {ActionType} from "@ant-design/pro-components";
import {CheckCircleOutline, ClockCircleOutline} from "antd-mobile-icons";
import {useInterval} from "ahooks";
import EditChartModal from "@/pages/Chart/MyChart/components/EditModel";
import {CHART_TYPE} from "@/constants/chart/chartType";
/**
 * 我的图表页面
 * @constructor
 */
const MyChartPage: React.FC = () => {
  const initSearchParams = {
    name: '',
    chartType: '',
    chartStatus:'',
    current: 1,
    pageSize: 4,
    sortField: 'createTime',
    sortOrder: 'desc',
  };

  const [searchParams, setSearchParams] = useState<API.ChartQueryRequest>({ ...initSearchParams });
  const { initialState } = useModel('@@initialState');
  const { currentUser } = initialState ?? {};
  const [chartList, setChartList] = useState<API.Chart[]>();
  const [total, setTotal] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [chartId, setChartId] = useState<number>(0);
  const actionRef = useRef<ActionType>();
  const [updateData, setUpdateData] = useState<API.Chart>({});
  const [editModalVisible, setEditModalVisible] = useState<boolean>(false);
  const [disableOption, setDisableOption] = useState<boolean>(false);
  const [chartTypeValue, setChartTypeValue] = useState('');
  const [statusValue, setStatusValue] = useState('');
  const [visible, setVisible] = useState(false);

  const loadData = async () => {
    console.log(searchParams);
    setLoading(true);
    try {
      const res = await listMyChartByPageUsingPost(searchParams);
      if (res.data) {
        setChartList(res.data.records ?? []);
        setTotal(res.data.total ?? 0);
        // 隐藏图表的 title
        if (res.data.records) {
          res.data.records.forEach((data) => {
            if (data.chartStatus === 'succeed') {
              const chartOption = JSON.parse(data.genChart ?? '{}');
              // 将图表配置中的 title 设置为 undefined
              chartOption.title = undefined; // 隐藏图标标题
              // 转换为字符串
              data.genChart = JSON.stringify(chartOption);
              console.log(chartOption);
            }
          });
          // 打印信息：
          console.log(res.data.records);
        }
      } else {
        message.error('获取我的图表失败');
      }
    } catch (e: any) {
      message.error('获取我的图表失败，' + e.message);
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, [searchParams]);

  //每60s加载一次数据
  useInterval(() => {
    loadData();
  }, 60000);
  /**
   * 搜索设置
   */
  const [query, setQuery] = useState('');

  const handleInputChange = (event: { target: { value: React.SetStateAction<string> } }) => {
    setQuery(event.target.value);
  };

  const handleSearchButtonClick = () => {
    handleSearch(query);
  };
  const handleChartTypeChange = (value: React.SetStateAction<any>) => {
    setChartTypeValue(value);
    setSearchParams({ ...searchParams, chartType: value });
  };

  const handleStatusChange = (value: React.SetStateAction<any>) => {
    setStatusValue(value);
    setSearchParams({ ...searchParams, chartStatus: value });
  };
  const handleReset = () => {
    setSearchParams({
      name: '',
      chartType: '',
      chartStatus: '',
      current: 1,
      pageSize: 4,
      sortField: 'createTime',
      sortOrder: 'desc',
    });
    setQuery('');
    setChartTypeValue('');
    setStatusValue('');
  };
  const handleSearch = (value: string) => {
    setSearchParams({ ...searchParams, name: value});
  };
  /**
   * 删除图表数据
   * @param params
   */
  const doDelete = async (params: number) => {
    if (params <= 0 || params === undefined) {
      message.error('图表id为空');
      return;
    }
    const data = {
      id: params,
    };
    try {
      const res = await deleteChartUsingPost(data);
      if (res.code === 0 && data) {
        message.success('删除成功');
        setChartId(0);
        // 重新加载数据
        loadData();
      } else {
        message.error('删除失败');
      }
    } catch (e: any) {
      message.error('删除失败', e.message);
    }
  };

  const reloadChart = async (id: number) => {
    if (id === undefined) {
      message.error('id为空');
      return;
    }
    console.log(id);
      const res = await regenerateChartUsingGet({id});
      if (res.code === 0) {
        message.success('请求成功，图表生成中请稍后');
        setChartId(0);
        loadData();
      } else {
        message.error('重新生成图表失败');
      }

    return;
  };
  const handleDelete = () => {
    Modal.confirm({
      title: '确认删除图表',
      content: '确认要删除该图表吗？',
      okType: 'danger',
      okText: '确认删除',
      cancelText: '取消',
      onOk() {
        console.log(`Deleting chart with ID: ${chartId}`);
        // 删除图表数据
        doDelete(chartId);
      },
    });
  };
  function handleRegenerate() {
    reloadChart(chartId);
  }

  const items = [
    {
      key: 'viewOriginalData',
      label: (
        <Space>
          <EyeOutlined />
          <div>原始数据</div>
        </Space>
      ),
      onClick: () => {
        // target: '_blank' 或者是 target: '_self'
        window.open(`/chart/data/${chartId}`, '_self');
      },
    },
    {
      key: 'edit',
      label: (
        <Space>
          <EditOutlined />
          <div>编辑</div>
        </Space>
      ),
      onClick: () => {
        setUpdateData(chartList?.find((item) => item.id === chartId) ?? {});
        setEditModalVisible(true);
      },
    },
    {
      key: 'regenerate',
      label: (
        <Space>
          <RedoOutlined />
          <div>重新生成</div>
        </Space>
      ),
      onClick: handleRegenerate,
    },
    {
      key: 'deleteChart',
      danger: true,
      label: (
        <Space>
          <DeleteOutlined />
          <div>删除</div>
        </Space>
      ),
      onClick: handleDelete,
    },

  ];

  function handleClick(id: any, id1: any) {
    console.log('设置' + id1);
    setChartId(id1);
    setVisible(!visible);
  }

  return (
    <div className="my-chart-page">
      <div >
        <Select
          style={{ width: '150px', marginLeft: '16px' }}
          value={chartTypeValue}
          onChange={handleChartTypeChange}
        >
          <Select.Option value="">请选择图表类型</Select.Option>
          <Select options={CHART_TYPE} />
        </Select>
        <Select
          style={{ width: '150px', marginLeft: '16px' }}
          value={statusValue}
          onChange={handleStatusChange}
        >
          <Select.Option value="">请选择状态</Select.Option>
          <Select.Option value="succeed">
            <CheckCircleOutline/> 成功
          </Select.Option>
          <Select.Option value="failed">
           <CheckCircleOutline /> 失败
          </Select.Option>
          <Select.Option value="wait">
            <ClockCircleOutline/> 等待中
          </Select.Option>
          <Select.Option value="running">
            <SmallDashOutlined/> 生成中
          </Select.Option>
        </Select>
        <Input
          style={{ width: '20%', marginLeft: '16px' }}
          placeholder="请输入图表名称"
          value={query}
          onChange={handleInputChange}
          allowClear
        />
        <Button
          type="primary"
          loading={loading}
          onClick={handleSearchButtonClick}
          style={{ marginLeft: '5px' }}
        >
          搜索
        </Button>
        <Button
          style={{ marginLeft: '16px' }}
          onClick={handleReset}
          type="primary"
          icon={<ReloadOutlined />}
        >
          清空
        </Button>
        <Button  onClick={loadData} type="default" size={'middle'} style={{float: 'right'}} icon={<RedoOutlined />}>
          刷新
        </Button>

      </div>
      <div className="margin-16" />
      <List
        grid={{
          gutter: 16,
          xs: 1,
          sm: 1,
          md: 1,
          lg: 2,
          xl: 2,
          xxl: 2,
        }}
        pagination={{
          onChange: (page, pageSize) => {
            setSearchParams({
              ...searchParams,
              current: page,
              pageSize,
            });
          },
          current: searchParams.current,
          pageSize: searchParams.pageSize,
          total: total,
        }}
        loading={loading}
        dataSource={chartList}
        renderItem={(item) => (
          <List.Item key={item.id}>
            <Card style={{ width: '100%' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <List.Item.Meta
                  avatar={<Avatar src={currentUser && currentUser.userAvatar} />}
                  title={item.name}
                  description={
                    item.chartType ? ( // 如果存在图表类型，则显示带有颜色的描述文本
                      <span style={{ color: '#747d8c'}}>图表类型：{item.chartType}</span> // 应用对应的颜色到描述文本上
                    ) : undefined // 如果不存在图表类型则不显示描述文本和颜色
                  }

                />
                <Dropdown menu={{ items }} trigger={['click']}>
                  <a onClick={(event) => handleClick(event, item.id)}>
                    <Space>
                    更多
                      <DownOutlined />
                    </Space>
                  </a>
                </Dropdown>
              </div>

              <>
                {item.chartStatus === 'wait' && (
                  <>
                    <Result
                      status="warning"
                      title="待生成"
                      subTitle={item.execMessage ?? '当前图表生成队列繁忙，请耐心等候'}
                    />
                  </>
                )}
                {item.chartStatus === 'running' && (
                  <>
                    <Result status="info" title="图表生成中" subTitle={item.execMessage}/>
                  </>
                )}
                {item.chartStatus === 'succeed' && (
                  <>
                    <div style={{marginBottom: 16}}/>
                    <p>{'分析目标：' + item.goal}</p>
                    <div style={{marginBottom: 16}}/>
                    <ReactECharts option={item.genChart && JSON.parse(item.genChart)}/>
                  </>
                )}
                {item.chartStatus === 'failed' && (
                  <>
                    <Result status="error" title="图表生成失败" subTitle={item.execMessage}/>
                  </>
                )}
                <div>
                  <Collapse
                    bordered={false}
                    items={[
                      {
                        key: item.id,
                        label: 'AI分析结论',
                        children: <p>{item.genResult}</p>,
                      },
                    ]}
                  />
                </div>
              </>
              <EditChartModal
                visible={editModalVisible}
                onCancel={() => setEditModalVisible(false)}
                onSubmit={(values) => {
                  // 保存修改的代码逻辑
                  setEditModalVisible(false);
                  loadData(); // 重新加载数据
                }}
                chartData={updateData}
              />
            </Card>
          </List.Item>
        )}
      />
    </div>
  );
};
export default MyChartPage;
