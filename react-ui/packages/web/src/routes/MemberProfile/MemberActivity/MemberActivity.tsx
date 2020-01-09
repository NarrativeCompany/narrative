import * as React from 'react';
import { MemberActivityMessages } from '../../../shared/i18n/MemberActivityMessages';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import { compose } from 'recompose';
import { MemberActivityLedgerEntryList } from './MemberActivityLedgerEntryList';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../shared/context/MemberProfileContext';
import { SEO } from '../../../shared/components/SEO';

type Props =
  WithMemberProfileProps &
  InjectedIntlProps;

const MemberActivityComponent: React.SFC<Props> = (props) => {
  const {
    isForCurrentUser,
    detailsForProfile: {user: {displayName}},
    intl: { formatMessage }
  } = props;

  const descriptionMessage = isForCurrentUser
    ? MemberActivityMessages.PageHeaderDescriptionForCurrentUser
    : MemberActivityMessages.PageHeaderDescription;

  return (
    <React.Fragment>
      <SEO
        title={formatMessage(MemberActivityMessages.PageHeaderTitle, {displayName})}
        description={descriptionMessage}
      />

      <MemberActivityLedgerEntryList {...props}/>
    </React.Fragment>
  );
};

const MemberActivity = compose(
  MemberProfileConnect,
  injectIntl
)(MemberActivityComponent) as React.ComponentClass<{}>;

export default MemberActivity;
