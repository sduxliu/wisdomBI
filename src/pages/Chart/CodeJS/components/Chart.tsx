import { useEffect, useRef } from "react";
import * as echarts from "echarts";

// @ts-ignore
function Chart({ options }) {
  const chartRef = useRef(null);
  let chartInstance: echarts.ECharts | null = null;

  function renderChart() {
    try {
      // @ts-ignore
      const renderedInstance = echarts.getInstanceByDom(chartRef.current);
      if (renderedInstance) {
        chartInstance = renderedInstance;
      } else {
        chartInstance = echarts.init(chartRef.current);
      }
      chartInstance.setOption(options);
    } catch (error) {
      // @ts-ignore
      console.error("error", error.message);
      chartInstance && chartInstance.dispose();
    }
  }

  function resizeHandler() {
    // @ts-ignore
    chartInstance.resize();
  }

  useEffect(() => {
    renderChart();
    return () => {
      chartInstance && chartInstance.dispose();
    };
  }, [options]);

  useEffect(() => {
    window.addEventListener("resize", resizeHandler);
    return () => window.removeEventListener("resize", resizeHandler);
  }, []);

  return (
    <div>
      <div style={{ height: "400px" }} ref={chartRef} />
    </div>
  );
}

export default Chart;
