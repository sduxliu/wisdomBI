// ChartEditPage.tsx

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Card, Input, Space } from 'antd';
import ReactECharts from 'echarts-for-react';
import {getChartByIdUsingGet, updateChartByGenUsingPost} from '@/services/wisdomBI/chartController';

const { TextArea } = Input;

const ChartEditPage: React.FC = () => {
  const { id } = useParams<{ id: any }>();
  const navigate = useNavigate();
  const [chartOption, setChartOption] = useState<any>({});
  const [dataString, setDataString] = useState<string>('');

  useEffect(() => {
    // Mock fetch function to get initial data
    const fetchData = async () => {
      // Replace with actual API call to fetch chart data by ID
      console.log(id);
      const res = await getChartByIdUsingGet({id});
      const  data = res.data;
      console.log(data);
      if(data === undefined){
        alert('未找到该图表');
        navigate('/my-chart');
      }
      // @ts-ignore
      setChartOption(JSON.parse(data.genChart))
      // @ts-ignore
      setDataString(data.genChart);
    };
    fetchData();
  }, [id]);

  const handleChartOptionChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    try {
      const newOption = JSON.parse(e.target.value);
      setChartOption(newOption);
      setDataString(e.target.value);
    } catch (error) {
      console.error('Invalid JSON format');
    }
  };

  const handleSubmit = async () => {
    try {
      await updateChartByGenUsingPost({
        id,
        genChart: dataString,
      });
      navigate('/my-chart'); // Redirect to the charts page or any other page
    } catch (error) {
      console.error('Failed to save chart data:', error);
    }
  };

  return (
    <div style={{ display: 'flex', padding: '16px' }}>
      <Card title="图表配置" style={{ width: '50%', marginRight: '16px' }}>
        <TextArea
          rows={20}
          value={dataString}
          onChange={handleChartOptionChange}
          placeholder="请输入图表配置（Echarts V5 选项配置）"
        />
        <Button
          type="primary"
          onClick={handleSubmit}
          style={{ marginTop: '16px' }}
        >
          保存
        </Button>
      </Card>
      <Card title="图表渲染" style={{ width: '50%' }}>
        <ReactECharts option={chartOption} />
      </Card>
    </div>
  );
};

export default ChartEditPage;


