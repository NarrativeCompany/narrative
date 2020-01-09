import * as React from 'react';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { compose } from 'recompose';
import { NicheDetailsConnect, WithNicheDetailsContextProps } from '../components/NicheDetailsContext';
import { NicheRenewalSection } from './components/NicheRenewalSection';
import { NicheModeratorManagementSection } from './components/NicheModeratorManagementSection';
import { NicheProfileSection } from './components/NicheProfileSection';
import { SEO } from '../../../../shared/components/SEO';
import { NicheSettingsMessages } from '../../../../shared/i18n/NicheSettingsMessages';

type Props =
  WithNicheDetailsContextProps &
  InjectedIntlProps;

const NicheSettingsComponent: React.SFC<Props> = (props) => {
  const { intl: {formatMessage} } = props;
  const nicheName = props.nicheDetail.niche.name;

  return (
    <React.Fragment>
      <SEO title={formatMessage(NicheSettingsMessages.SettingsSeoTitle, {nicheName})} />
      <NicheRenewalSection {...props}/>
      <NicheProfileSection {...props}/>
      <NicheModeratorManagementSection {...props}/>
    </React.Fragment>
  );
};

export default compose(
  injectIntl,
  NicheDetailsConnect,
)(NicheSettingsComponent) as React.ComponentClass<{}>;
