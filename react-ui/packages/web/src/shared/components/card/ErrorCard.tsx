import * as React from 'react';
import { ErrorBody, ErrorBodyParentProps } from '../ErrorBody';
import { CardProps, StyledCard } from '../Card';
import styled from '../../styled';

type ParentProps = CardProps &
  ErrorBodyParentProps;

// jw: let's center and contain the card to a limited width
export const CardContainer = styled.div`
  margin: 20px auto;
  max-width: 550px;
  
  h1 {
    text-align: center;
  }
`;

// jw: unlike our other cards I'm not going to be exposing this one statically from Card. It should not be needed enough
//     to warrant it.

export const ErrorCard: React.SFC<ParentProps> = (props) => {
  const { title, titleType, description, gifType, extraInfo, ...cardProps } = props;
  const bodyProps = { title, titleType, description, gifType, extraInfo };

  return (
    <CardContainer>
      <StyledCard {...cardProps}>
        <ErrorBody {...bodyProps}/>
      </StyledCard>
    </CardContainer>
  );
};
