import * as React from 'react';
import { compose, withProps } from 'recompose';
import { WithPublicationDetailsContextProps } from '../../../components/PublicationDetailsContext';
import { withPublicationProfile, WithPublicationProfileProps } from '@narrative/shared';
import { fullPlaceholder, withLoadingPlaceholder } from '../../../../../shared/utils/withLoadingPlaceholder';
import { AppealPublicationToTribunalSection } from './components/AppealPublicationToTribunalSection';
import { PublicationRoleList } from './components/PublicationRoleList';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { WriterRevenueShareSection } from './components/WriterRevenueShareSection';

const PublicationInfoBodyComponent: React.SFC<
  WithPublicationProfileProps
  & WithPublicationDetailsContextProps> = (props) => {

  const { publicationProfile, publicationDetail, publicationDetail: { publication} } = props;

  return (
    <React.Fragment>
      <Paragraph marginBottom="large">
        {publication.description}
      </Paragraph>
      {publicationDetail.owner &&
        <PublicationRoleList
          title={PublicationDetailsMessages.PublicationRoleOwner}
          excludeUserCount={true}
          users={[publicationDetail.owner]}
        />
      }
      <PublicationRoleList
        title={PublicationDetailsMessages.PublicationRoleAdministrators}
        users={publicationProfile.admins}
      />
      <PublicationRoleList
        title={PublicationDetailsMessages.PublicationRoleEditors}
        users={publicationProfile.editors}
      />
      <PublicationRoleList
        title={PublicationDetailsMessages.PublicationRoleWriters}
        users={publicationProfile.writers}
      />
      <div style={{marginTop: 20}}>
        <WriterRevenueShareSection profile={publicationProfile} />
      </div>
      <div style={{marginTop: 40}}>
        <AppealPublicationToTribunalSection publicationProfile={publicationProfile} />
      </div>
    </React.Fragment>
  );
};

export const PublicationInfoBody = compose(
  withProps((props: WithPublicationDetailsContextProps) => {
    const { publicationDetail: { oid } } = props;

    return {publicationOid: oid};
  }),
  withPublicationProfile,
  withLoadingPlaceholder(fullPlaceholder)
)(PublicationInfoBodyComponent) as React.ComponentClass<WithPublicationDetailsContextProps>;
