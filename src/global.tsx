import '@umijs/max';
import { Button, message, notification } from 'antd';
import defaultSettings from '../config/defaultSettings';

const { pwa } = defaultSettings;
const isHttps = document.location.protocol === 'https:';
const clearCache = () => {
  // 移除所有缓存
  if (window.caches) {
    caches
      .keys()
      .then((keys) => {
        keys.forEach((key) => {
          caches.delete(key);
        });
      })
      .catch((e) => console.log(e));
  }
};

// 如果PWA启用了
if (pwa) {
  // 当网络离线时通知用户
  window.addEventListener('sw.offline', () => {
    message.warning('当前处于离线状态');
  });

  // 提示页面上有新内容并询问用户是否想用最新版本
  window.addEventListener('sw.updated', (event: Event) => {
    const e = event as CustomEvent;
    const reloadSW = async () => {
      // 检查是否有在等待的Service Worker
      const worker = e.detail && e.detail.waiting;
      if (!worker) {
        return true;
      }
      // 通过MessageChannel向等待中的SW发送 skip-waiting 事件
      await new Promise((resolve, reject) => {
        const channel = new MessageChannel();
        channel.port1.onmessage = (msgEvent) => {
          if (msgEvent.data.error) {
            reject(msgEvent.data.error);
          } else {
            resolve(msgEvent.data);
          }
        };
        worker.postMessage(
          {
            type: 'skip-waiting',
          },
          [channel.port2],
        );
      });
      // 清除缓存并刷新页面
      clearCache();
      window.location.reload();
      return true;
    };
    const key = `open${Date.now()}`;
    const btn = (
      <Button
        type="primary"
        onClick={() => {
          notification.destroy(key);
          reloadSW();
        }}
      >
        {'刷新'}
      </Button>
    );
    // 显示通知消息，提示用户页面有新内容
    notification.open({
      message: '有新内容',
      description: '请点击“刷新”按钮或者手动刷新页面',
      btn,
      key,
      onClose: async () => null,
    });
  });
} else if ('serviceWorker' in navigator && isHttps) {
  // 注销Service Worker
  const { serviceWorker } = navigator;
  if (serviceWorker.getRegistrations) {
    serviceWorker.getRegistrations().then((sws) => {
      sws.forEach((sw) => {
        sw.unregister();
      });
    });
  }
  serviceWorker.getRegistration().then((sw) => {
    if (sw) sw.unregister();
  });
  clearCache();
}
