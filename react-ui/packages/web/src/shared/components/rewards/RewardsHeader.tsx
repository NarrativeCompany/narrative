import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { RewardsMessages } from '../../i18n/RewardsMessages';
import styled from '../../styled';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { Heading } from '../Heading';
import { Link } from '../Link';
import { Block } from '../Block';
import { themeColors } from '../../styled/theme';
import { mediaQuery } from '../../styled/utils/mediaQuery';

export interface RewardsHeaderProps {
  title: React.ReactNode;
  description: React.ReactNode;
  rightColumn: React.ReactNode;
}

type ParentProps = RewardsHeaderProps;

export const RewardsHeaderWrapper = styled<FlexContainerProps>(FlexContainer)`
  flex-direction: row;
  padding-bottom: 20px;
  border-bottom: 1px solid #dfdfdf;
  margin-bottom: 20px;

  ${mediaQuery.md_down`
    flex-direction: column;
  `};
`;

const RewardsRightColumnWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-left: 30px;
  
  ${mediaQuery.md_down`
    margin-top: 20px;
    margin-left: 0;
    justify-content: center;
  `};
`;

export const RewardsHeaderBody = styled<FlexContainerProps>(FlexContainer)`
  flex-direction: column;
`;

export const RewardsHeader: React.SFC<ParentProps> = (props) => {
  const { title, description, rightColumn } = props;

  return (
    <RewardsHeaderWrapper>
      <RewardsHeaderBody>
        {title &&
          <Heading size={3} color={themeColors.primaryGreen}>
            {title}
          </Heading>
        }
        <Block size="large">
          {description}
          {' '}
          <Link.About type="rewards" size="inherit">
            <FormattedMessage {...RewardsMessages.LearnMoreAboutNarrativeRewards}/>
          </Link.About>
        </Block>
      </RewardsHeaderBody>
      {rightColumn &&
        <RewardsRightColumnWrapper>
          {rightColumn}
        </RewardsRightColumnWrapper>
      }
    </RewardsHeaderWrapper>
  );
};
