import * as React from 'react';
import { compose } from 'recompose';
import { Col, Row } from 'antd';
import { ViewWrapper } from '../../shared/components/ViewWrapper';
import { PageHeader } from '../../shared/components/PageHeader';
import { SEO } from '../../shared/components/SEO';
import { CardLink, cardLinks } from '../../shared/components/CardLink';
import { SuggestNicheButton } from './components/SuggestNicheButton';
import { FormattedMessage, } from 'react-intl';
import { HQLandingMessages } from '../../shared/i18n/HQLandingMessages';
import { SEOMessages } from '../../shared/i18n/SEOMessages';
import styled from '../../shared/styled';
import { CreatePublicationButton } from './components/CreatePublicationButton';

const PageHeaderWrapper = styled.div`
  margin: 25px auto;
  max-width: 470px;
`;

export const HQLanding: React.SFC<{}> = () => {
  const description = (
    <React.Fragment>
      <FormattedMessage {...HQLandingMessages.PageHeaderDescriptionIntro}/>
      <br/><br/>
      <FormattedMessage {...HQLandingMessages.PageHeaderDescription}/>
    </React.Fragment>
  );

  return (
    <ViewWrapper>
      <SEO
        title={SEOMessages.HQTitle}
        description={SEOMessages.HQDescription}
      />

      <PageHeaderWrapper>
        <PageHeader
          center="all"
          preTitle={<FormattedMessage {...HQLandingMessages.PageHeaderPreTitle}/>}
          title={<FormattedMessage {...HQLandingMessages.PageHeaderTitle}/>}
          description={description}
          extra={
            <React.Fragment>
              <SuggestNicheButton/>
              <CreatePublicationButton/>
            </React.Fragment>
            }
        />
      </PageHeaderWrapper>

      <Row gutter={28} type="flex" justify="center">
        {cardLinks.map(cardLink => (
          <Col key={cardLink} xs={24} sm={12} md={12} lg={8} style={{marginBottom: 28}}>
            <CardLink type={cardLink}/>
          </Col>
        ))}
      </Row>
    </ViewWrapper>
  );
};

export default compose()(HQLanding);
