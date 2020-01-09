import * as React from 'react';
import { Heading } from '../Heading';
import { FormattedMessage } from 'react-intl';
import styled from '../../styled';

const VotePointsContainer = styled.span`
  margin-left: 6px;
  color: ${props => props.theme.textColorLight};
  text-transform: capitalize;
`;

interface Props {
  title: FormattedMessage.MessageDescriptor;
  pointsMessage: FormattedMessage.MessageDescriptor;
  formattedPoints: JSX.Element;
}

export const ReferendumVoteColumnHeading: React.SFC<Props> = (props) => {
  const { title, pointsMessage, formattedPoints } = props;

  return (
    <Heading size={6} uppercase={true}>
      <FormattedMessage {...title} />
      <VotePointsContainer>
        (<FormattedMessage {...pointsMessage} values={{ formattedPoints }}/>)
      </VotePointsContainer>
    </Heading>
  );
};
