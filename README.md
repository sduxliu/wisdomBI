# 智汇数据BI

## 项目简介

本项目是基于React+Spring Boot+RabbitMQ+AIGC的智能BI数据分析平台。

区别于传统的BI，用户（数据分析者）只需要导入最原始的数据集，输入想要进行分析的目标（比如帮
我分析一下网站的增长趋势)，就能利用AI自动生成一个符合要求的图表以及分析结论。此外，还会有图
表管理、异步生成等功能。

**优势：** 让不会数据分析的用户也可以通过输入目标快速完成数据分析，大幅节约人力成本，将会用到 AI 接口生成分析结果



## 项目背景

1. 基于AI快速发展的时代，AI + 程序员 = 无限可能。
2. 传统数据分析流程繁琐：传统的数据分析过程需要经历繁琐的数据处理和可视化操作，耗时且复杂。
3. 技术要求高：传统数据分析需要数据分析者具备一定的技术和专业知识，限制了非专业人士的参与。
4. 人工成本高：传统数据分析需要大量的人力投入，成本昂贵。
5. AI自动生成图表和分析结论：该项目利用AI技术，只需导入原始数据和输入分析目标，即可自动生成符合要求的图表和分析结论。、
6. 提高效率降低成本：通过项目的应用，能够大幅降低人工数据分析成本，提高数据分析的效率和准确性。



## 项目技术栈与特点



### 后端

1. Spring Boot 2.7.2
2. Spring MVC
3. MyBatis + MyBatis Plus 数据访问（开启分页）
4. Spring Boot 调试工具和项目处理器
5. Spring AOP 切面编程
6. Spring Scheduler 定时任务
7. Spring 事务注解
8. Redis：Redisson限流控制
9. MyBatis-Plus 数据库访问结构
10. IDEA插件 MyBatisX ： 根据数据库表自动生成
11. **RabbitMQ：消息队列**
12. AI SDK：
13. JDK 线程池及异步化
14. Swagger + Knife4j 项目文档
15. Easy Excel：表格数据处理、Hutool工具库 、Apache Common Utils、Gson 解析库、Lombok 注解

### 前端

1. React 18
2. Umi 4 前端框架
3. Ant Design Pro 5.x 脚手架
4. Ant Design 组件库
5. OpenAPI 代码生成：自动生成后端调用代码（来自鱼聪明开发平台）
6. EChart 图表生成

### 数据存储

- MySQL 数据库
- 阿里云 OSS 对象存储

### 项目特性

- Spring Session Redis 分布式登录
- 全局请求响应拦截器（记录日志）
- 全局异常处理器
- 自定义错误码
- 封装通用响应类
- Swagger + Knife4j 接口文档
- 自定义权限注解 + 全局校验
- 全局跨域处理
- 长整数丢失精度解决
- 多环境配置



## 项目架构图



### 基础架构

基础架构：客户端输入分析诉求和原始数据，向业务后端发送请求。业务后端利用AI服务处理客户端数据，保持到数据库，并生成图表。处理后的数据由业务后端发送给AI服务，AI服务生成结果并返回给后端，最终将结果返回给客户端展示。
[![image.png](doc/img_infrastructure.png)]

!上图的流程会出现一个问题：
假设一个AI服务生成图表和分析结果要等50秒，如果有大量用户需要生成图表，每个人都需要等待50秒，那么AI
服务可能无法承受这种压力。为了解决这个问题，**可以采用消息队列技术**。

同样地，通过消息队列，用户可以提交生成图表的请求，这些请求会进入队列，AI服务会依次处理队列中的请求，从而避免了同时处理大量请求造成的压力，同时也能更好地控制资源的使用。
### 优化项目架构-异步化处理

**优化：**

优化流程（异步化：）
客户端输入分析诉求和原始数据，向业务后端发送清求。业务后端将清求事件放入消息队
列，并为客户端生成消息号码，让要生成图表的客户端去排队，消息队列根据AI服务负载情况，定期检查进度，如果AI服务还能处理更多的图表生成请求，就向任务处理模块发送消息。
任务处理模块调用AI服务处理客户端数据，A!服务异步生成结果返回给后端并保存到数据库，当后端的AI服务生成
完毕后，可以通过向前端发送通知的方式，或者通过业务后端监控数据库中图表生成服务的状态，来确定生成结果是否可用。若生成结果可用，前端即可获取并处理相应的数据，最终将结果返回给客户端展示。（在此期间，用户可以去做自己的事情)
[![image.png](doc/img_optimizedArchitecture.png)]

## 项目启动

### 前端

#### Install `node_modules`:

```
npm install 
```

OR

```
yarn
```

#### 启动项目

```3#
npm start
```

#### 构建项目

```
npm run dev
```



### 后端

1. 下载/拉取本项目到本地
2. 通过 IDEA 代码编辑器进行打开项目，等待依赖的下载
3. 修改配置文件 `application.yaml` 的信息，比如数据库、Redis、RabbitMQ等
4. 修改信息完成后，一键行运行项目



## 项目核心亮点

1. 自动化分析：通过AI技术，将传统繁琐的数据处理和可视化操作自动化，使得数据分析过程更加高效、快速和准确。
2. 一键生成：只需要导入原始数据集和输入分析目标，系统即可自动生成符合要求的可视化图表和分析结论，无需手动进行复杂的操作和计算。
3. 可视化管理：项目提供了图表管理功能，可以对生成的图表进行整理、保存和分享，方便用户进行后续的分析和展示。
4. 异步生成：项目支持异步生成，即使处理大规模数据集也能保持较低的响应时间，提高用户的使用体验和效率。

