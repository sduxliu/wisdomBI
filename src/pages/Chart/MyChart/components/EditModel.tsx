// EditChartModal.tsx

import React, { useState } from 'react';
import { Modal, Form, Input, Select, message } from 'antd';
import { regenerateChartUsingGet } from '@/services/wisdomBI/chartController';
import {CHART_TYPE} from "@/constants/chart/chartType";

interface EditChartModalProps {
  visible: boolean;
  onCancel: () => void;
  onSubmit: (values: any) => void;
  chartData: API.Chart;
}

const EditChartModal: React.FC<EditChartModalProps> = ({
                                                         visible,
                                                         onCancel,
                                                         onSubmit,
                                                         chartData,
                                                       }) => {
  const [form] = Form.useForm();

  const handleFinish = async (values: any) => {
    Modal.confirm({
      title: '是否重新生成图表？',
      content: '修改已保存，您是否需要重新生成图表？',
      okText: '是',
      cancelText: '否',
      onOk: async () => {
        const res = await regenerateChartUsingGet({ id: chartData.id });
        if (res.code === 0) {
          message.success('图表重新生成成功');
        } else {
          message.error('图表重新生成失败');
        }
        onSubmit(values);
      },
      onCancel: () => {
        onSubmit(values);
      },
    });
  };

  return (
    <Modal
      title="编辑图表"
      visible={visible}
      onCancel={onCancel}
      onOk={() => form.submit()}
    >
      <Form form={form} initialValues={chartData} onFinish={handleFinish}>
        <Form.Item name="name" label="图表名称" rules={[{ required: true, message: '请输入图表名称' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="chartType" label="图表类型" rules={[{ required: true, message: '请选择图表类型' }]}>
          <Select placeholder="请输选择图表类型" options={CHART_TYPE} />
        </Form.Item>
        <Form.Item name="goal" label="分析目标" rules={[{ required: true, message: '请输入分析目标' }]}>
          <Input.TextArea />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default EditChartModal;
