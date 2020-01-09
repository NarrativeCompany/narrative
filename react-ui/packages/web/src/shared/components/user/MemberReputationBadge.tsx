import * as React from 'react';
import { generatePath } from 'react-router';
import { User, ReputationLevel, UserReputation } from '@narrative/shared';
import { AnchorSizeType, Link } from '../Link';
import { themeColors } from '../../styled/theme';
import { WebRoute } from '../../constants/routes';
import styled, { css } from '../../../shared/styled';

interface NoMarginRightProp {
  noMarginRight?: boolean;

}

const badgeColor = {
  [ReputationLevel.CONDUCT_NEGATIVE]: themeColors.primaryRed,
  [ReputationLevel.HIGH]: themeColors.brightGreen,
  [ReputationLevel.MEDIUM]: themeColors.greyBlue,
  [ReputationLevel.LOW]: themeColors.gold,
};

const getBadgeStylesByUserReputation = (reputation: UserReputation) => {
  const { kycVerifiedScore, level } = reputation;
  const borderRadius = kycVerifiedScore ? '4px' : 0;
  const borderProperty = kycVerifiedScore ? 'border' : 'border-bottom';

  return `
    border-radius: ${borderRadius};
    ${borderProperty}: 1px solid ${badgeColor[level]}
  `;
};

const ReputationBadge = styled<{reputation: UserReputation} & NoMarginRightProp>(
  ({reputation, noMarginRight, ...props}) => <span {...props}/>
)`
    padding: 0 5px;
    margin: 0 5px;
    ${p => p.noMarginRight && css`
      margin-right: 0;
    `};
    line-height: 1.25;
    
    // reputation based styles
    ${p => getBadgeStylesByUserReputation(p.reputation)};
    
    // anchor overrides 
    a {
      color: ${p => p.theme.textColorLight} !important;
      line-height: 1.25;
    }
  `;

interface MemberBadgeProps extends NoMarginRightProp {
  user: User;
  badgeSize?: AnchorSizeType;
}

export const MemberReputationBadge: React.SFC<MemberBadgeProps> = (props) => {
  const { user, badgeSize, noMarginRight } = props;
  const { username, reputation } = user;

  if (user.deleted || username === '' || !reputation) {
    return null;
  }

  return (
    <ReputationBadge reputation={reputation} noMarginRight={noMarginRight}>
      <Link size={badgeSize} to={generatePath(WebRoute.UserProfileReputation, { username })}>
        {reputation.totalScore}
      </Link>
    </ReputationBadge>
  );
};
