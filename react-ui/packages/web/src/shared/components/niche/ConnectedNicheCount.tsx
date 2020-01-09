import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { Paragraph } from '../Paragraph';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { PostMessages } from '../../i18n/PostMessages';
import styled from '../../styled';

const totalAllowedNicheConnections = 3;

const ConnectedNicheCountWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-left: 15px;
  height: 50px;
  
  span,
  p {
    line-height: 14px;
    margin: 0;
  }
  
  @media screen and (max-width: 575px) {
    margin-left: 0;
  }
`;

const CountBar = styled<{isSelected: boolean}, 'div'>('div')`
  height: 4px;
  width: 24px;
  background: ${p => p.isSelected ? p.theme.brightGreen : p.theme.borderGrey};
  
  &:not(:last-child) {
    margin-right: 8px;
  }
`;

interface ConnectedNicheCountProps {
  totalCount: number;
  successMessage?: FormattedMessage.MessageDescriptor;
}

export const ConnectedNicheCount: React.SFC<ConnectedNicheCountProps> = (props) => {
  const { totalCount } = props;
  const message = getNicheCountMessage(props);

  return (
    <ConnectedNicheCountWrapper column={true} justifyContent="center">
      <FlexContainer style={{paddingBottom: 10}}>
        {new Array(totalAllowedNicheConnections).fill(null).map((_, i) => {
          const isSelected = (i + 1) <= totalCount;

          return <CountBar key={i} isSelected={isSelected}/>;
        })}
      </FlexContainer>

      <Paragraph color="light">
        <FormattedMessage {...message}/>
      </Paragraph>
    </ConnectedNicheCountWrapper>
  );
};

function getNicheCountMessage (props: ConnectedNicheCountProps) {
  const { successMessage, totalCount } = props;

  const success = successMessage || PostMessages.ConnectNicheCountCompleteText;

  return totalCount < totalAllowedNicheConnections ? {
    ...PostMessages.ConnectNicheCountText,
    values: {
      totalCount,
      totalMissing: totalAllowedNicheConnections - totalCount,
    }
  } : {
    ...success
  };
}
