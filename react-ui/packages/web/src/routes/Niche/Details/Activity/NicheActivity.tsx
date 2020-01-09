import * as React from 'react';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { compose } from 'recompose';
import { SEO } from '../../../../shared/components/SEO';
import { NicheDetailsConnect, WithNicheDetailsContextProps } from '../components/NicheDetailsContext';
import { NicheDetailsMessages } from '../../../../shared/i18n/NicheDetailsMessages';
import { NicheHistorySection } from './History/NicheHistorySection';
import { NicheActions } from './Actions/NicheActions';

type Props =
  WithNicheDetailsContextProps &
  InjectedIntlProps;

const NicheActivityComponent: React.SFC<Props> = (props) => {
  const { nicheDetail, intl: { formatMessage } } = props;

  const nicheName = nicheDetail.niche.name;

  return (
    <React.Fragment>
      <SEO title={formatMessage(NicheDetailsMessages.ActivitySeoTitle, {nicheName})} />

      <NicheActions nicheDetail={nicheDetail} />

      <NicheHistorySection nicheDetail={nicheDetail} />
    </React.Fragment>
  );
};

export default compose(
  NicheDetailsConnect,
  injectIntl
)(NicheActivityComponent) as React.ComponentClass<{}>;
