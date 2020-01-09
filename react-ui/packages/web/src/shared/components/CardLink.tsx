import * as React from 'react';
import { compose, withProps } from 'recompose';
import { Link } from './Link';
import { Card, CardProps } from './Card';
import { FlexContainer } from '../styled/shared/containers';
import { FormattedMessage } from 'react-intl';
import { CardLinkMessages } from '../i18n/CardLinkMessages';
import { WebRoute } from '../constants/routes';
import { Heading, HeadingProps } from './Heading';
import { Paragraph } from './Paragraph';
import { CustomIcon, IconType } from './CustomIcon';
import styled from '../styled/index';

const StyledCard = styled<CardProps>(Card)`
  .ant-card-body {
    padding: 42px 24px;
    
    a {
      position:relative;
      z-index: 1;
    }
  }
  
  a.cardLink {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 0;
  }
  
`;

const IconWrapper = styled.div`
  margin-bottom: 20px;
`;

const Title = styled<HeadingProps>(Heading)`
  margin-bottom: 20px;
`;

export const cardLinks: CardLinkType[] = [
  'review',
  'bid',
  'moderators',
  'leadership',
  'appeals',
  'network-stats'
];

type CardLinkType = 'review' | 'bid' | 'leadership' | 'appeals' | 'moderators' | 'network-stats';

interface WithProps {
  cardDefaults: CardDefaults;
}

interface ParentProps {
  type: CardLinkType;
}

type CardLinkProps =
  ParentProps &
  WithProps;

const CardLinkComponent: React.SFC<CardLinkProps> = (props) => {
  const { cardDefaults } = props;

  return (
    <StyledCard height={250} style={{position: 'relative'}}>
      <FlexContainer column={true} centerAll={true}>
        <IconWrapper>
          <CustomIcon type={cardDefaults.icon} size="lg"/>
        </IconWrapper>

        <Title size={4}>{cardDefaults.title}</Title>

        <Paragraph textAlign="center">{cardDefaults.description}</Paragraph>
      </FlexContainer>

      <Link to={cardDefaults.route} className="cardLink"/>
    </StyledCard>
  );
};

interface CardDefaults {
  icon: IconType;
  title: React.ReactNode;
  description: React.ReactNode;
  route: WebRoute;
}

function getNicheLink(singular: boolean) {
  return <Link.About type={singular ? 'niche' : 'niches'} />;
}

function getCardDefaults (type: CardLinkType): CardDefaults {

  switch (type) {
    case 'review':
      return {
        icon: type,
        title: <FormattedMessage {...CardLinkMessages.ApprovalTitle}/>,
        description: <FormattedMessage {...CardLinkMessages.ApprovalDescription} values={{Niche: getNicheLink(true)}}/>,
        route: WebRoute.Approvals
      };
    case 'bid':
      return {
        icon: type,
        title: <FormattedMessage {...CardLinkMessages.AuctionsTitle}/>,
        description: <FormattedMessage {...CardLinkMessages.AuctionsDescription} values={{Niche: getNicheLink(true)}}/>,
        route: WebRoute.Auctions
      };
    case 'leadership':
      return {
        icon: type,
        title: <FormattedMessage {...CardLinkMessages.TribunalTitle}/>,
        description: <FormattedMessage {...CardLinkMessages.TribunalDescription}/>,
        route: WebRoute.LeadershipTribunal
      };
    case 'appeals':
      return {
        icon: type,
        title: <FormattedMessage {...CardLinkMessages.AppealsTitle}/>,
        description: (
          <FormattedMessage
            {...CardLinkMessages.AppealsDescription}
            values={{Niches: getNicheLink(false)}}
          />
        ),
        route: WebRoute.Appeals
      };
    case 'moderators':
      return {
        icon: type,
        title: <FormattedMessage {...CardLinkMessages.ModeratorsTitle}/>,
        description: (
          <FormattedMessage
            {...CardLinkMessages.ModeratorsDescription}
            values={{Niche: getNicheLink(true)}}
          />
        ),
        route: WebRoute.Moderators
      };
    case 'network-stats':
      return {
        icon: type,
        title: <FormattedMessage {...CardLinkMessages.NetworkStatsTitle}/>,
        description: (
          <FormattedMessage
            {...CardLinkMessages.NetworkStatsDescription}
            values={{Niches: getNicheLink(false)}}
          />
        ),
        route: WebRoute.NetworkStats
      };
    default:
      throw new Error('getCardDefaults: card type must be provided');
  }
}

export const CardLink = compose(
  withProps((props: CardLinkProps) => {
    const { type } = props;
    const cardDefaults = getCardDefaults(type);

    return { cardDefaults };
  })
)(CardLinkComponent) as React.ComponentClass<ParentProps>;
