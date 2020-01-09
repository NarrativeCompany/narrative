import * as React from 'react';
import { branch, compose, renderComponent } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { SEO } from '../../../shared/components/SEO';
import { MemberNeoWalletMessages } from '../../../shared/i18n/MemberNeoWalletMessages';
import { MemberNeoWalletBody } from './MemberNeoWalletBody';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import { Link } from '../../../shared/components/Link';
import { WebRoute } from '../../../shared/constants/routes';
import {
  WithCurrentUserProps,
  withExtractedCurrentUser, WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';
import { generatePath } from 'react-router';
import { ContainedLoading } from '../../../shared/components/Loading';
import { Paragraph } from '../../../shared/components/Paragraph';
import { PointRedemptionExplanation } from '../../MemberProfile/MemberRewards/components/PointRedemptionExplanation';

type Props = WithCurrentUserProps;

const MemberNeoWallet: React.SFC<Props> = (props) => {
  const { currentUser : { username } } = props;

  const rewardPointsLink = (
    <Link to={generatePath(WebRoute.UserProfileRewards, {username})}>
      <FormattedMessage {...MemberNeoWalletMessages.RewardPointsLinkText}/>
    </Link>
  );
  const nrveLink = <Link.About type="nrve" />;

  const description = (
    <React.Fragment>
      <Paragraph marginBottom="large">
        <FormattedMessage
          {...MemberNeoWalletMessages.MemberNeoWalletDescParagraphOneIntro}
          values={{rewardPointsLink, nrveLink}}
        />
        <PointRedemptionExplanation />
      </Paragraph>
      <Paragraph marginBottom="large">
        <FormattedMessage {...MemberNeoWalletMessages.MemberNeoWalletDescParagraphTwo}/>
      </Paragraph>
    </React.Fragment>
  );

  return (
    <React.Fragment>
      <SEO title={MemberNeoWalletMessages.MemberNeoWalletSeoTitle} />

      <SectionHeader
        title={<FormattedMessage {...MemberNeoWalletMessages.NeoAddress}/>}
        description={description}
      />

      <MemberNeoWalletBody />
    </React.Fragment>
  );
};

export default compose(
  withExtractedCurrentUser,
  branch<WithExtractedCurrentUserProps>(({currentUserLoading}) => !!currentUserLoading,
    renderComponent(() => <ContainedLoading/>)
  )
)(MemberNeoWallet) as React.ComponentClass<{}>;
