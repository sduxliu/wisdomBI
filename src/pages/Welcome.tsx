import { PageContainer } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { Card, theme } from 'antd';
import React from 'react';

/**
 * 每个单独的卡片，为了复用样式抽成了组件
 * @param param0
 * @returns
 */
const InfoCard: React.FC<{
  title: string;
  index: number;
  desc: string;
  href: string;
}> = ({ title, href, index, desc }) => {
  const { useToken } = theme;

  const { token } = useToken();

  return (
    <div
      style={{
        backgroundColor: token.colorBgContainer,
        boxShadow: token.boxShadow,
        borderRadius: '8px',
        fontSize: '14px',
        color: token.colorTextSecondary,
        lineHeight: '22px',
        padding: '16px 19px',
        minWidth: '220px',
        flex: 1,
      }}
    >
      <div
        style={{
          display: 'flex',
          gap: '4px',
          alignItems: 'center',
        }}
      >
        <div
          style={{
            width: 48,
            height: 48,
            lineHeight: '22px',
            backgroundSize: '100%',
            textAlign: 'center',
            padding: '8px 16px 16px 12px',
            color: '#FFF',
            fontWeight: 'bold',
            backgroundImage:
              "url('https://gw.alipayobjects.com/zos/bmw-prod/daaf8d50-8e6d-4251-905d-676a24ddfa12.svg')",
          }}
        >
          {index}
        </div>
        <div
          style={{
            fontSize: '16px',
            color: token.colorText,
            paddingBottom: 8,
          }}
        >
          {title}
        </div>
      </div>
      <div
        style={{
          fontSize: '14px',
          color: token.colorTextSecondary,
          textAlign: 'justify',
          lineHeight: '22px',
          marginBottom: 8,
        }}
      >
        {desc}
      </div>
      <a href={href} target="_blank" rel="noreferrer">
        了解更多 {'>'}
      </a>
    </div>
  );
};

const Welcome: React.FC = () => {
  const { token } = theme.useToken();
  const { initialState } = useModel('@@initialState');

  return (
    <PageContainer>
      <Card
        style={{
          borderRadius: 8,
        }}
        bodyStyle={{
          backgroundImage:
          // @ts-ignore
            initialState?.settings?.navTheme === 'realDark'
              ? 'background-image: linear-gradient(75deg, #1A1B1F 0%, #191C1F 100%)'
              : 'background-image: linear-gradient(75deg, #FBFDFF 0%, #F5F7FF 100%)',
        }}
      >
        <div
          style={{
            backgroundPosition: '100% -30%',
            backgroundRepeat: 'no-repeat',
            backgroundSize: '274px auto',
            backgroundImage:
              "url('https://gw.alipayobjects.com/mdn/rms_a9745b/afts/img/A*BuFmQqsB2iAAAAAAAAAAAAAAARQnAQ')",
          }}
        >
          <div
            style={{
              fontSize: '20px',
              color: token.colorTextHeading,
              marginBottom: 16,
              marginLeft: '30px',
              width: '100%',
            }}
          >
            欢迎使用 智汇数据平台
          </div>
          <p
            style={{
              fontSize: '14px',
              color: token.colorTextSecondary,
              lineHeight: '22px',
              marginTop: 16,
              marginBottom: 32,
              width: '65%',
            }}
          >
            智汇数据平台是一个融合了先进性、便捷性和智能化的数据分析解决方案。在这个数据驱动的时代背景下，它为各大企业提供了一个全新的数据处理和分析方式，从而帮助企业做出明智的决策。
            <br />
            <strong>主要特点：</strong>
            <ul>
              <li>
                <strong>数据收集能力：</strong>轻松汇集来自各个渠道的原始数据。
              </li>
              <li>
                <strong>智能化分析功能：</strong>
                无需复杂分析工具，用户只需导入数据集并设定目标，平台会利用AI技术自动生成图表与结论。
              </li>
              <li>
                <strong>可视化呈现：</strong>通过直观的图表以及分析结论，快速了解数据背后的故事。
              </li>
              <li>
                <strong>异步生分析：</strong>支持异步生成分析结果，提高使用灵活性和效率。
              </li>
              <li>
                <strong>图表管理便捷：</strong>用户可轻松管理和调整自己的数据图表。
              </li>
            </ul>
          </p>
          {/*<div*/}
          {/*  style={{*/}
          {/*    display: 'flex',*/}
          {/*    flexWrap: 'wrap',*/}
          {/*    gap: 16,*/}
          {/*  }}*/}
          {/*>*/}
          {/*  <InfoCard*/}
          {/*    index={1}*/}
          {/*    href="https://umijs.org/docs/introduce/introduce"*/}
          {/*    title="了解 umi"*/}
          {/*    desc="umi 是一个可扩展的企业级前端应用框架,umi 以路由为基础的，同时支持配置式路由和约定式路由，保证路由的功能完备，并以此进行功能扩展。"*/}
          {/*  />*/}
          {/*  <InfoCard*/}
          {/*    index={2}*/}
          {/*    title="了解 智汇数据平台"*/}
          {/*    href="https://ant.design"*/}
          {/*    desc="antd 是基于 智汇数据平台 设计体系的 React UI 组件库，主要用于研发企业级中后台产品。"*/}
          {/*  />*/}
          {/*  <InfoCard*/}
          {/*    index={3}*/}
          {/*    title="了解 Pro Components"*/}
          {/*    href="https://procomponents.ant.design"*/}
          {/*    desc="ProComponents 是一个基于 智汇数据平台 做了更高抽象的模板组件，以 一个组件就是一个页面为开发理念，为中后台开发带来更好的体验。"*/}
          {/*  />*/}
          {/*</div>*/}
        </div>
      </Card>
    </PageContainer>
  );
};

export default Welcome;
