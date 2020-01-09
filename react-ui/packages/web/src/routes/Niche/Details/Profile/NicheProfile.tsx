import * as React from 'react';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { compose } from 'recompose';
import { NicheDetailsConnect, WithNicheDetailsContextProps } from '../components/NicheDetailsContext';
import { EnhancedNicheStatus } from '../../../../shared/enhancedEnums/nicheStatus';
import { NicheOwnerSection } from './components/NicheOwnerSection';
import { NicheModeratorsSection } from './components/NicheModeratorsSection';
import { AppealNicheToTribunalSection } from './components/AppealNicheToTribunalSection';
import { SEO } from '../../../../shared/components/SEO';
import { NicheProfileMessages } from '../../../../shared/i18n/NicheProfileMessages';

type Props =
  WithNicheDetailsContextProps &
  InjectedIntlProps;

const NicheProfileComponent: React.SFC<Props> = (props) => {
  const { nicheDetail, intl: {formatMessage} } = props;
  const nicheName = nicheDetail.niche.name;

  const nicheStatus = EnhancedNicheStatus.get(nicheDetail.niche.status);

  return (
    <React.Fragment>
      <SEO title={formatMessage(NicheProfileMessages.SeoTitle, {nicheName})} />
      {nicheStatus.isActive() && <NicheOwnerSection {...props}/>}
      {nicheStatus.isActive() && <NicheModeratorsSection {...props}/>}

      <AppealNicheToTribunalSection {...props}/>
    </React.Fragment>
  );
};

export default compose(
  injectIntl,
  NicheDetailsConnect
)(NicheProfileComponent) as React.ComponentClass<{}>;
