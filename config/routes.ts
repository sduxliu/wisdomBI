export default [
  { path: '/user', layout: false, routes:
      [
        { name: '登录', path: '/user/login', component: './User/Login' },
        { name: '注册', path: '/user/register', component: './User/Register' },
      ],
  },
  {
    path: '/welcome',
    name: '首页',
    icon: 'smile',
    component: './Welcome',
  },
  {
    path: '/chart',
    name: 'AI智能分析',
    icon: 'lineChart',
    routes: [
      { path: '/chart', redirect: '/chart/add_chart' },
      { path: '/chart/add_chart', name: '智能分析(同步）', component: './Chart/AddChart' },
      { path: '/chart/add_chart_async', name: '智能分析（异步线程池）', component: './Chart/AddChartAsync' },
      { path: '/chart/add_chart_asyncMq', name: '智能分析(异步MQ)',  component: './Chart/AddChartAsyncMQ' },
    ]
  },

  // { path: '/', redirect: '/add_chart' },
  // { path: '/add_chart', name: '智能分析', icon: 'lineChart', component: './Chart/AddChart' },
  // {
  //   path: '/add_chart_async',
  //   name: '智能分析（异步线程池）',
  //   icon: 'barChart',
  //   component: './Chart/AddChartAsync',
  // },
  // {
  //   path: '/add_chart_asyncMq',
  //   name: '智能分析(异步MQ)',
  //   icon: 'barChart',
  //   component: './Chart/AddChartAsyncMQ',
  // },
  { path: '/chartCode', name: '图表渲染', icon: 'edit', component: './Chart/CodeJS'},
  { path: '/my_chart', name: '我的图表', icon: 'pieChart', component: './Chart/MyChart' },
  { name: '个人中心', path: '/user/info', hideInMenu: true, component: './User/UserInfo'},
  // 携带参数
  { path: '/chart/data/:id', name: '图表生成数据', hideInMenu: true, component: './Chart/ChartData'},
  {
    path: '/admin',
    name: '管理',
    icon: 'crown',
    access: 'canAdmin',
    routes: [
      {
        name: '用户管理',
        path: '/admin/user',
        component: 'Admin/Users',
      },
      {
        name: '图表管理',
        path: '/admin/chart',
        component: 'Admin/Chart',
      },
    ],
  },
  { path: '/', redirect: '/welcome' },
  { path: '*', layout: false, component: './404' },
];
