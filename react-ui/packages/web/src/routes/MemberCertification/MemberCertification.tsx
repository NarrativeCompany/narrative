import * as React from 'react';
import { Redirect } from 'react-router';
import { branch, compose, renderComponent } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { ViewWrapper } from '../../shared/components/ViewWrapper';
import { SEO } from '../../shared/components/SEO';
import { PageHeader } from '../../shared/components/PageHeader';
import { CustomIcon } from '../../shared/components/CustomIcon';
import { Card } from '../../shared/components/Card';
import { MemberCertificationForm } from './components/MemberCertificationForm';
import { MemberCertificationFormMessages } from '../../shared/i18n/MemberCertificationFormMessages';
import { FlexContainer, FlexContainerProps } from '../../shared/styled/shared/containers';
import { UserKycStatus, withCurrentUserKyc, WithCurrentUserKycProps } from '@narrative/shared';
import { WebRoute } from '../../shared/constants/routes';
import styled from '../../shared/styled';
import { viewWrapperPlaceholder, withLoadingPlaceholder } from '../../shared/utils/withLoadingPlaceholder';

const PageHeaderWrapper = styled<FlexContainerProps>(FlexContainer)`
  img {
    margin-bottom: 15px;
  }
`;

const MemberCertification: React.SFC<{}> = () => {
  return (
    <ViewWrapper style={{ paddingTop: 40 }}>
      <SEO title={MemberCertificationFormMessages.PageHeaderTitle}/>

      <Card>
        <PageHeaderWrapper centerAll={true} column={true}>
          <CustomIcon type="approve" size={45}/>

          <PageHeader
            title={<FormattedMessage {...MemberCertificationFormMessages.PageHeaderTitle}/>}
            size={2}
            center="all"
          />
        </PageHeaderWrapper>

        <MemberCertificationForm/>
      </Card>
    </ViewWrapper>
  );
};

export default compose(
  withCurrentUserKyc,
  withLoadingPlaceholder(viewWrapperPlaceholder()),
  branch((props: WithCurrentUserKycProps) => props.userKyc.kycStatus !== UserKycStatus.READY_FOR_VERIFICATION,
    renderComponent(() => <Redirect to={WebRoute.MemberCertification}/>)
  )
)(MemberCertification) as React.ComponentClass<{}>;
