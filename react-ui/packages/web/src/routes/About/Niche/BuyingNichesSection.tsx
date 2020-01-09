import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { AboutSection } from '../components/AboutSection';
import { NicheExplainerMessages } from '../../../shared/i18n/NicheExplainerMessages';
import { Link } from '../../../shared/components/Link';
import { externalUrls } from '../../../shared/constants/externalUrls';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';
import { WebRoute } from '../../../shared/constants/routes';

export const BuyingNichesSection: React.SFC<{}> = () => {
  const hereLink = (
    <Link.Anchor href={WebRoute.Auctions} target="_blank">
      <FormattedMessage {...NicheExplainerMessages.Here}/>
    </Link.Anchor>
  );
  const buyNicheExplainerLink = (
    <Link.Anchor href={externalUrls.narrativeHowToBuyANiche} target="_blank">
      <FormattedMessage {...NicheExplainerMessages.BuyNicheExplainerLink}/>
    </Link.Anchor>
  );
  return (
    <AboutSection title={NicheExplainerMessages.BuyingNichesSectionTitle} titleType="niche">
      <AboutSectionParagraph
        message={NicheExplainerMessages.BuyingNichesParagraphOne}
        values={{hereLink}}
      />
      <AboutSectionParagraph message={NicheExplainerMessages.BuyingNichesParagraphTwo}/>
      <AboutSectionParagraph
        message={NicheExplainerMessages.BuyingNichesParagraphThree}
        values={{buyNicheExplainerLink}}
      />
    </AboutSection>
  );
};
