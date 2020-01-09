import * as React from 'react';
import { compose, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { Heading } from '../../../shared/components/Heading';
import { HighlightColor, HighlightedCard, HighlightSide } from '../../../shared/components/HighlightedCard';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { Paragraph } from '../../../shared/components/Paragraph';
import { ReputationLevel, UserReputation } from '@narrative/shared';
import { themeColors } from '../../../shared/styled/theme';
import { MemberReputationMessages } from '../../../shared/i18n/MemberReputationMessages';
import { WithUserReputationProps } from './MemberReputation';
import styled from '../../../shared/styled';
import { CountDown } from '../../../shared/components/CountDown';
import * as moment from 'moment-timezone';
import { LocalizedTime } from '../../../shared/components/LocalizedTime';
import { Link } from '../../../shared/components/Link';
import { WebRoute } from '../../../shared/constants/routes';

const CardBodyWrapper = styled<FlexContainerProps>(FlexContainer)`
  @media screen and (max-width: 575px) {
    flex-direction: column;
    
    h1 {
      margin-bottom: 20px;
    }
  }
`;

const TitleAndDescWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-left: 32px;
  
  @media screen and (max-width: 575px) {
    align-items: center;
    margin: 0;
    
    p {
      text-align: center;
    }
  }
`;

interface WithProps {
  highlightColor: HighlightColor;
  highlightSide: HighlightSide;
  titleColor: string;
  title: FormattedMessage.Props;
  description?: FormattedMessage.MessageDescriptor;
  conductNegativeReputationGetKycMessage?: FormattedMessage.MessageDescriptor;
  totalScore: number;
  conductNegativeExpirationElement: JSX.Element;
}

export const ReputationScoreCardComponent: React.SFC<WithProps> = (props) => {
  const {
    highlightColor, highlightSide, titleColor, title,
    description, conductNegativeReputationGetKycMessage, totalScore, conductNegativeExpirationElement
  } = props;

  const certifiedLink = (
    <Link to={WebRoute.MemberCertification}>
      <FormattedMessage {...MemberReputationMessages.KycCertifiedLink}/>
    </Link>
  );

  return (
    <HighlightedCard
      highlightColor={highlightColor}
      highlightSide={highlightSide}
      highlightWidth={3}
      style={{ marginBottom: 60 }}
      bodyStyle={{ padding: 32 }}
      noBoxShadow={true}
    >
      <CardBodyWrapper alignItems="center">
        <Heading size={1} noMargin={true} weight={400}>{totalScore}</Heading>

        <TitleAndDescWrapper column={true}>
          <Heading size={4} color={titleColor}>
            <FormattedMessage {...title}/>
          </Heading>

          {description &&
          <Paragraph marginBottom="default">
            <FormattedMessage {...description}/>
          </Paragraph>}

          {conductNegativeExpirationElement}

          {conductNegativeReputationGetKycMessage &&
          <Paragraph>
            <FormattedMessage {...conductNegativeReputationGetKycMessage} values={{certifiedLink}}/>
          </Paragraph>}

        </TitleAndDescWrapper>
      </CardBodyWrapper>
    </HighlightedCard>
  );
};

export function getUserReputationScoreCardProps(userReputation: UserReputation, isForCurrentUser: boolean) {
  const { level, kycVerifiedScore, kycVerificationPending } = userReputation;

  let description;
  let conductNegativeReputationGetKycMessage;

  switch (level) {
    case ReputationLevel.LOW:
    case ReputationLevel.MEDIUM:
      if (isForCurrentUser) {
        if (kycVerifiedScore) {
          description = MemberReputationMessages.LowMediumReputationKYCMessageForCurrentUser;
        } else if (kycVerificationPending) {
          const certificationLink = (
            <Link to={WebRoute.MemberCertification}>
              <FormattedMessage {...MemberReputationMessages.KycCertificationLink}/>
            </Link>
          );
          description = {
            ...MemberReputationMessages.LowMediumReputationPendingKYCMessage,
            values: {certification: certificationLink}
          };
        } else {
          const certifyingLink = (
            <Link to={WebRoute.MemberCertification}>
              <FormattedMessage {...MemberReputationMessages.KycCertifyingLink}/>
            </Link>
          );
          description = {
            ...MemberReputationMessages.LowMediumReputationNoKYCMessage,
            values: {certifying: certifyingLink}
          };
        }
      }

      return {
        highlightColor: level === ReputationLevel.LOW ? 'gold' : 'grey-blue',
        titleColor: level === ReputationLevel.LOW ? themeColors.gold : themeColors.greyBlue,
        title: level === ReputationLevel.LOW ?
          MemberReputationMessages.LowReputationTitle :
          MemberReputationMessages.MediumReputationTitle,
        description
      };

    case ReputationLevel.HIGH:
      return {
        highlightColor: 'bright-green',
        titleColor: themeColors.brightGreen,
        title: MemberReputationMessages.HighReputationTitle,
        description: MemberReputationMessages.HighReputationMessage
      };

    case ReputationLevel.CONDUCT_NEGATIVE:
      description = isForCurrentUser ?
        MemberReputationMessages.ConductNegativeReputationMessageForCurrentUser :
        MemberReputationMessages.ConductNegativeReputationMessage;
      if (!kycVerifiedScore && isForCurrentUser) {
        conductNegativeReputationGetKycMessage = MemberReputationMessages.ConductNegativeReputationGetKYCMessage;
      }
      return {
        highlightColor: 'red',
        titleColor: themeColors.primaryRed,
        title: MemberReputationMessages.ConductNegativeReputationTitle,
        description,
        conductNegativeReputationGetKycMessage
      };
    default:
      throw new Error('getReputationScoreCardProps: reputation level not provided');
  }
}

export function getNegativeConductExpirationElement(negativeConductExpirationTimestamp: string | null,
                                                    conductNegative: boolean | null) {
  const timestamp = negativeConductExpirationTimestamp;

  if ((conductNegative && timestamp != null)) {
    const now = moment(moment.now());
    const timestampMoment = moment(timestamp);
    const hourDiff = timestampMoment.diff(now, 'hours');

    if (hourDiff < 72) {
      return <CountDown endTime={timestamp} timeOnly={true}/>;
    } else {
      return <LocalizedTime time={timestamp}/>;
    }
  } else {
    return null;
  }
}

export const ReputationScoreCard = compose(
  withProps((props: WithUserReputationProps) => {
    const { kycVerifiedScore, totalScore, negativeConductExpirationTimestamp, conductNegative } = props.userReputation;

    const highlightSide = kycVerifiedScore ? 'all' : 'bottom';
    const restCardProps = getUserReputationScoreCardProps(props.userReputation, props.isForCurrentUser);

    const conductNegativeExpirationTime =
      getNegativeConductExpirationElement(negativeConductExpirationTimestamp, conductNegative);

    const conductNegativeExpirationElement = conductNegativeExpirationTime &&
      (
        <Paragraph marginBottom="default">
          <FormattedMessage
            {...MemberReputationMessages.ConductNegativePenaltyTimeExpires}
            values={{expiration: conductNegativeExpirationTime}}/>
        </Paragraph>
      );

    return { highlightSide, totalScore, conductNegativeExpirationElement, ...restCardProps };
  })
)(ReputationScoreCardComponent) as React.ComponentClass<WithUserReputationProps>;
