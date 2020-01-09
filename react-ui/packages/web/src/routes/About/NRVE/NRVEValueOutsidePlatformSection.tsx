import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { AboutSection } from '../components/AboutSection';
import { NRVEExplainerMessages } from '../../../shared/i18n/NRVEExplainerMessages';
import { Link } from '../../../shared/components/Link';
import { externalUrls } from '../../../shared/constants/externalUrls';

export const NRVEValueOutsidePlatformSection: React.SFC<{}> = () => {
  const switcheoLink = <Link.TokenSale type="switcheo"/>;
  const bilaxyLink = <Link.TokenSale type="bilaxy"/>;
  const coinMarketCapLink = (
    <Link.Anchor href={externalUrls.narrativeCoinMarketCap} target="_blank">
      <FormattedMessage {...NRVEExplainerMessages.CoinMarketCapLink}/>
    </Link.Anchor>
  );

  return (
    <AboutSection
      title={NRVEExplainerMessages.NRVEValueOutsidePlatformSectionTitle}
      titleType="nrve"
      message={NRVEExplainerMessages.NRVEValueOutsidePlatformParagraphOne}
      messageValues={{ switcheoLink, bilaxyLink, coinMarketCapLink }}
    />
  );
};
