import * as React from 'react';
import { AboutSection } from '../components/AboutSection';
import { NicheExplainerMessages } from '../../../shared/i18n/NicheExplainerMessages';
import { Link } from '../../../shared/components/Link';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';

export const NicheOriginSection: React.SFC<{}> = () => {
  const termsOfServiceLink = <Link.Legal type="tos"/>;

  return (
    <AboutSection title={NicheExplainerMessages.NicheOriginSectionTitle} titleType="niches">
      <AboutSectionParagraph message={NicheExplainerMessages.NicheOriginSectionParagraphOne}/>
      <AboutSectionParagraph
        message={NicheExplainerMessages.NicheOriginSectionParagraphTwo}
        values={{ termsOfServiceLink}}
      />
      <AboutSectionParagraph message={NicheExplainerMessages.NicheOriginSectionParagraphThree}/>
    </AboutSection>
  );
};
