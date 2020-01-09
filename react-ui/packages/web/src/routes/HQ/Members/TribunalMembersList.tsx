import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import { Col, Row } from 'antd';
import { TribunalMembersListItem } from './TribunalMembersListItem';
import { PageHeader } from '../../../shared/components/PageHeader';
import { SEO } from '../../../shared/components/SEO';
import { User, withAllTribunalMembers, WithAllTribunalMembersProps } from '@narrative/shared';
import { TribunalListItemMessages } from '../../../shared/i18n/TribunalMembersListMessages';
import { FormattedMessage } from 'react-intl';
import { SEOMessages } from '../../../shared/i18n/SEOMessages';
import { Loading } from '../../../shared/components/Loading';

interface WithProps {
  members: User[];
  loading: boolean;
}

type Props =
  WithAllTribunalMembersProps &
  WithProps;

const TribunalMemberList: React.SFC<Props> = (props) => {
  const { members } = props;

  if (!members) {
    return null;
  }

  return (
    <React.Fragment>
      <SEO
        title={SEOMessages.TribunalTitle}
        description={SEOMessages.TribunalDescription}
      />
      <PageHeader
        preTitle={<FormattedMessage {...TribunalListItemMessages.PageHeaderPreTitle}/>}
        title={<FormattedMessage {...TribunalListItemMessages.PageHeaderTitle}/>}
        description={<FormattedMessage {...TribunalListItemMessages.PageHeaderDescription}/>}
        iconType="leadership"
      />
      <Row gutter={28} style={{marginTop: 50}}>
        {members.map((member, i) =>
          (<Col xl={6} lg={6} md={8} sm={12} xs={24}><TribunalMembersListItem key={i} member={member}/></Col>))
        }
      </Row>
    </React.Fragment>
  );
};

export default compose(
  withAllTribunalMembers,
  branch((props: WithAllTribunalMembersProps) => props.allTribunalMembersData.loading,
    renderComponent(() => <Loading/>)
  ),
  withProps((props: WithAllTribunalMembersProps) => {
  const { allTribunalMembersData } = props;

  const loading =
    allTribunalMembersData &&
    allTribunalMembersData.loading;
  const members =
    allTribunalMembersData &&
    allTribunalMembersData.getAllTribunalMembers || [];

  return { members, loading };
}),
)(TribunalMemberList) as React.ComponentClass<{}>;
