import * as React from 'react';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { DetailsGradientBox } from '../../../shared/components/DetailsGradientBox';
import { ViewWrapper } from '../../../shared/components/ViewWrapper';
import { AboutPageHeader } from '../components/AboutPageHeader';
import { compose } from 'recompose';
import { PublicationExplainerMessages } from '../../../shared/i18n/PublicationExplainerMessages';
import { WebRoute } from '../../../shared/constants/routes';
import { Link } from '../../../shared/components/Link';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';
import { AboutSection } from '../components/AboutSection';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { CreatePublicationButton } from '../../HQ/components/CreatePublicationButton';
import {
  withPermissionsModalHelpers,
  WithPermissionsModalHelpersProps
} from '../../../shared/containers/withPermissionsModalHelpers';
import { RevokeReasonMessages } from '../../../shared/i18n/RevokeReasonMessages';
import { PermissionErrorModal } from '../../../shared/components/PermissionErrorModal';

type Props = InjectedIntlProps &
  WithPermissionsModalHelpersProps;

const PublicationExplainerComponent: React.SFC<Props> = (props) => {
  const { intl: { formatMessage }, permissionLinkSecurer, permissionErrorModalProps } = props;

  const annualFeeRangeLink = (
    <Link to={WebRoute.CreatePublication} linkSecurer={permissionLinkSecurer}>
      <FormattedMessage {...PublicationExplainerMessages.AnnualFeePriceRange}/>
    </Link>
  );

  const nicheLink = <Link.About type="niche"/>;
  const nichesLink = <Link.About type="niches"/>;
  const acceptableUsePolicyLink = <Link.Legal type="aup"/>;
  const narrativeRewardsLink = <Link.About type="rewards"/>;

  return (
    <ViewWrapper
      maxWidth={890}
      style={{ paddingTop: 75 }}
      gradientBox={<DetailsGradientBox color="darkBlue" size="large"/>}
    >
      <AboutPageHeader
        seoTitle={formatMessage(PublicationExplainerMessages.SEOTitle)}
        title={<FormattedMessage {...PublicationExplainerMessages.PageHeaderTitle}/>}
        description={<FormattedMessage {...PublicationExplainerMessages.PageHeaderDescription}/>}
      />

      <AboutSection>
        <AboutSectionParagraph message={PublicationExplainerMessages.Description}/>

        <AboutSectionParagraph
          message={PublicationExplainerMessages.AnnualFeeDescription}
          values={{annualFeeRangeLink}}
        />
      </AboutSection>

      <AboutSection
        title={PublicationExplainerMessages.PowerUsers}
        message={PublicationExplainerMessages.PowerUsersDescription}
      />

      <AboutSection
        title={PublicationExplainerMessages.Rewards}
        message={PublicationExplainerMessages.RewardsDescription}
        messageValues={{narrativeRewardsLink, nicheLink}}
      />

      <AboutSection title={PublicationExplainerMessages.OwnershipRightsAndAppeals}>
        <AboutSectionParagraph message={PublicationExplainerMessages.OwnershipRightsAndAppealsDescriptionOne}/>

        <AboutSectionParagraph
          message={PublicationExplainerMessages.OwnershipRightsAndAppealsDescriptionTwo}
          values={{acceptableUsePolicyLink}}
        />
      </AboutSection>

      <AboutSection title={PublicationExplainerMessages.Branding}>
        <AboutSectionParagraph message={PublicationExplainerMessages.BrandingDescriptionOne}/>

        <AboutSectionParagraph
          message={PublicationExplainerMessages.BrandingDescriptionTwo}
          values={{nichesLink}}
        />

        <AboutSectionParagraph message={PublicationExplainerMessages.BrandingDescriptionThree}/>
      </AboutSection>

      <AboutSection>
        <AboutSectionParagraph asBlock={true}>
          <FlexContainer centerAll={true}>
            <CreatePublicationButton
              type="primary"
              size="large"
              btnText={PublicationExplainerMessages.StartYourOwnPublication}
            />
          </FlexContainer>
        </AboutSectionParagraph>
      </AboutSection>

      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps} />}
    </ViewWrapper>
  );
};

export default compose(
  injectIntl,
  withPermissionsModalHelpers('createPublications', RevokeReasonMessages.CreatePublication)
)(PublicationExplainerComponent) as React.ComponentClass<{}>;
