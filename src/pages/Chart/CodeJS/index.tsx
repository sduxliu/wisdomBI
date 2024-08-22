import React, { useState } from "react";
import Chart from "./components/Chart";
import {Button} from "antd";
import {UndoOutlined} from "@ant-design/icons";

const initialOptions = {
  title: {
    text: 'Stacked Line'
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['Email', 'Union Ads', 'Video Ads', 'Direct', 'Search Engine']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  toolbox: {
    feature: {
      saveAsImage: {}
    }
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      name: 'Email',
      type: 'line',
      stack: 'Total',
      data: [120, 132, 101, 134, 90, 230, 210]
    },
    {
      name: 'Union Ads',
      type: 'line',
      stack: 'Total',
      data: [220, 182, 191, 234, 290, 330, 310]
    },
    {
      name: 'Video Ads',
      type: 'line',
      stack: 'Total',
      data: [150, 232, 201, 154, 190, 330, 410]
    },
    {
      name: 'Direct',
      type: 'line',
      stack: 'Total',
      data: [320, 332, 301, 334, 390, 330, 320]
    },
    {
      name: 'Search Engine',
      type: 'line',
      stack: 'Total',
      data: [820, 932, 901, 934, 1290, 1330, 1320]
    }
  ]
};

function App() {
  const [options, setOptions] = useState(initialOptions);
  const [inputValue, setInputValue] = useState(JSON.stringify(initialOptions, null, 2));
  const [error, setError] = useState(null);

  const handleInputChange = (e: { target: { value: React.SetStateAction<string> } }) => {
    setInputValue(e.target.value);
    try {
      const parsedOptions = new Function('return ' + e.target.value)();
      setOptions(parsedOptions);
      setError(null);
    } catch (err) {
      // @ts-ignore
      setError(`Invalid JavaScript: ${err.message}`);
    }
  };

  const handleReset = () => {
    setOptions(initialOptions);
    setInputValue(JSON.stringify(initialOptions, null, 2));
    setError(null);
  };

  return (
    <div className="App" style={{ padding: "20px" }}>
      <h2>请根据示例输入配置信息</h2>
      <textarea
        style={{ width: "100%", height: "200px", marginBottom: "20px" }}
        value={inputValue}
        onChange={handleInputChange}
      />
      <Button type={"primary"}  icon={<UndoOutlined />}onClick={handleReset} style={{ marginBottom: "20px" }}>
        复原
      </Button>
      <div style={{ display: "flex", height: "400px" }}>
        <div style={{ width: "100%" }}>
          <Chart options={options} />
          {error && <div style={{ color: "red", marginTop: "10px" }}>{error}</div>}
        </div>
      </div>
    </div>
  );
}

export default App;
