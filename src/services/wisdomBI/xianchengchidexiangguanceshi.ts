// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** 添加任务 GET /api/queue/add */
export async function addUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.addUsingGETParams,
  options?: { [key: string]: any },
) {
  return request<any>('/api/queue/add', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 获取线程池信息 GET /api/queue/get */
export async function getUsingGet(options?: { [key: string]: any }) {
  return request<string>('/api/queue/get', {
    method: 'GET',
    ...(options || {}),
  });
}
