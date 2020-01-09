import * as React from 'react';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { MemberNichesDetails } from './components/MemberNichesDetails';
import { compose } from 'recompose';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../../shared/context/MemberProfileContext';
import { SEO } from '../../../../shared/components/SEO';
import { MemberChannelsMessages } from '../../../../shared/i18n/MemberChannelsMessages';

type Props =
  WithMemberProfileProps &
  InjectedIntlProps;

const MemberNichesComponent: React.SFC<Props> = (props) => {
  const {
    detailsForProfile: {user: {displayName}},
    intl: {formatMessage}
  } = props;
  return (
    <React.Fragment>
      <SEO title={formatMessage(MemberChannelsMessages.NichesTitle, {displayName})} />
      <MemberNichesDetails {...props} />
    </React.Fragment>
  );
};

const MemberNiches = compose(
  injectIntl,
  MemberProfileConnect,
)(MemberNichesComponent) as React.ComponentClass<{}>;

export default MemberNiches;
