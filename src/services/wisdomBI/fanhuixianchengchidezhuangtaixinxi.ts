// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** 获取线程池信息 GET /api/queue/get */
export async function getUsingGet(options?: { [key: string]: any }) {
  return request<string>('/api/queue/get', {
    method: 'GET',
    ...(options || {}),
  });
}
