import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { HighlightedCard, HighlightColor, HighlightedCardProps } from '../../../shared/components/HighlightedCard';
import { Heading } from '../../../shared/components/Heading';
import { Paragraph } from '../../../shared/components/Paragraph';
import { CustomIcon } from '../../../shared/components/CustomIcon';
import styled from '../../../shared/styled';

const StyledHighlightCard = styled<HighlightedCardProps>(HighlightedCard)`
  &:not(:last-child) {
    margin-bottom: 20px;
  }

  .ant-card-body {
    padding: 16px 24px;
  }
  
  @media screen and (max-width: 575px) { 
    .ant-card-body {
      padding: 16px; 12px;
    }
  }
`;

const StyledRow = styled<FlexContainerProps>(FlexContainer)`
  @media screen and (max-width: 575px) {
    flex-direction: column;
  }
`;

const ContentWrapper = styled<FlexContainerProps & {addMargin?: boolean}>(FlexContainer)`
  ${p => p.addMargin && `margin-right: 15px;`}
  
  @media screen and (max-width: 575px) {
    align-items: center;
    
    p {
      text-align: center;
    }
  }
`;

const FlexCol = styled<FlexContainerProps>(FlexContainer)`
  margin-left: auto;
  
   @media screen and (max-width: 575px) {
     margin: 20px 0 0 0;
     align-self: flex-start;
     justify-content: center;
     width: 100%;
   }
`;

interface ParentProps {
  color: HighlightColor;
  title: FormattedMessage.Props;
  description: FormattedMessage.Props;
  rating: number;
}

export const ReputationBreakdownCard: React.SFC<ParentProps> = (props) => {
  const { color, title, description, rating } = props;

  return (
    <StyledHighlightCard highlightColor={color} noBoxShadow={true}>
      <StyledRow alignItems="center">
        <ContentWrapper column={true} addMargin={true}>
          <Heading uppercase={true} size={5}>
            <FormattedMessage {...title}/>
          </Heading>

          <Paragraph>
            <FormattedMessage {...description}/>
          </Paragraph>
        </ContentWrapper>

        <FlexCol>
          <ContentWrapper addMargin={true}>
            <Heading size={2} weight={300} noMargin={true}>
              {rating}
            </Heading>
          </ContentWrapper>

          <ContentWrapper>
            <CustomIcon type={rating < 50 ? 'frown' : 'smile'}/>
          </ContentWrapper>
        </FlexCol>
      </StyledRow>
    </StyledHighlightCard>
  );
};
