import * as React from 'react';
import { PageHeader } from '../../../shared/components/PageHeader';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import whyGuy from '../../../assets/why-guy-blue.svg';
import styled from '../../../shared/styled';
import { SEO } from '../../../shared/components/SEO';

const HeaderWrapper = styled<FlexContainerProps>(FlexContainer)`
  max-width: 700px;
  width: 100%;
  color: #fff;
  position: relative;
  margin: 0 auto 80px;
  
  h1 {
    line-height: 50px;
    color: #fff;
    margin-right: 0;
  }
  
  div > span {
    color: #fff;
    font-size: 24px;
    font-weight: 300;
  }
  
  img {
    @media screen and (max-width: 575px) {
      width: 90%;
    }
  }
`;

interface ParentProps {
  seoTitle: string;
  seoDescription?: string;
  title: React.ReactNode;
  description: React.ReactNode;
}

export const AboutPageHeader: React.SFC<ParentProps> = (props) => {
  const { seoTitle, seoDescription, title, description } = props;

  return (
    <React.Fragment>
      <SEO title={seoTitle} description={seoDescription}/>

      <HeaderWrapper column={true} alignItems="center">
        <PageHeader
          title={title}
          description={description}
          center="all"
          style={{ marginBottom: 40 }}
        />

        <img src={whyGuy} alt=""/>
      </HeaderWrapper>
    </React.Fragment>
  );
};
