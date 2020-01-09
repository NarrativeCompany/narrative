import * as React from 'react';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { compose } from 'recompose';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../../shared/context/MemberProfileContext';
import { SEO } from '../../../../shared/components/SEO';
import { MemberChannelsMessages } from '../../../../shared/i18n/MemberChannelsMessages';
import { MemberPublicationsBody } from './components/MemberPublicationsBody';

type Props =
  WithMemberProfileProps &
  InjectedIntlProps;

const MemberPublicationsComponent: React.SFC<Props> = (props) => {
  const {
    detailsForProfile: {user: {displayName}},
    intl: {formatMessage}
  } = props;

  return (
    <React.Fragment>
      <SEO title={formatMessage(MemberChannelsMessages.PublicationsTitle, {displayName})} />

      <MemberPublicationsBody />
    </React.Fragment>
  );
};

const MemberPublications = compose(
  injectIntl,
  MemberProfileConnect,
)(MemberPublicationsComponent) as React.ComponentClass<{}>;

export default MemberPublications;
