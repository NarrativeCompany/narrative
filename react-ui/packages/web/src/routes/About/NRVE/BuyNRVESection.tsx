import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { AboutSection } from '../components/AboutSection';
import { NRVEExplainerMessages } from '../../../shared/i18n/NRVEExplainerMessages';
import { Link } from '../../../shared/components/Link';
import { externalUrls } from '../../../shared/constants/externalUrls';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';

export const BuyNRVESection: React.SFC<{}> = () => {
  const howToBuyNrveLink = (
    <Link.Anchor href={externalUrls.narrativeHowToBuyNrve} target="_blank">
      <FormattedMessage {...NRVEExplainerMessages.HowToBuyNrveLink}/>
    </Link.Anchor>
  );

  return (
    <AboutSection title={NRVEExplainerMessages.BuyNRVESectionTitle} titleType="nrve">
      <AboutSectionParagraph message={NRVEExplainerMessages.BuyNRVEParagraphOne} values={{ howToBuyNrveLink }}/>
      <AboutSectionParagraph message={NRVEExplainerMessages.BuyNRVEParagraphTwo}/>
    </AboutSection>
  );
};
