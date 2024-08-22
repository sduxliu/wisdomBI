import { ProColumns, ProTable } from '@ant-design/pro-components';
import { message, Modal } from 'antd';
import React, { PropsWithChildren } from 'react';
import {addChartUsingPost} from "@/services/wisdomBI/chartController";

interface CreateModalProps {
  modalVisible: boolean;
  columns: ProColumns<API.Chart>[];
  onSubmit: () => void;
  onCancel: () => void;
}

/**
 * 添加节点
 * @param fields
 */
const handleAdd = async (fields: API.Chart) => {
  const hide = message.loading('正在添加');
  try {
    await addChartUsingPost({ ...fields } as API.ChartAddRequest);
    hide();
    message.success('添加成功');
    // 关闭

    return true;
  } catch (error) {
    hide();
    message.error('添加失败请重试！');
    return false;
  }
};

/**
 * 创建数据模态框
 * @param props
 * @constructor
 */
const CreateChartModal: React.FC<PropsWithChildren<CreateModalProps>> = (props) => {
  const { modalVisible, columns, onSubmit, onCancel } = props;

  return (
    <Modal
      destroyOnClose
      title="新建"
      open={modalVisible}
      onCancel={() => onCancel()}
      footer={null}
    >
      <ProTable<API.Chart, API.Chart>
        onSubmit={async (value) => {
          const success = await handleAdd(value);
          if (success) {
            onSubmit?.();
          }
        }}
        rowKey="id"
        type="form"
        columns={columns}
      />
    </Modal>
  );
};

export default CreateChartModal;
