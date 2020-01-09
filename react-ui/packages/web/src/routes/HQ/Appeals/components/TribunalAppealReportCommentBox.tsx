import * as React from 'react';
import styled from '../../../../shared/styled';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { commentBodyStyles } from '../../../../shared/styled/shared/comment';

const ChatBubble = styled.div`
  border: 1px solid #40a9ff;
  border-radius: 10px;
  color: #40a9ff;
  padding: 10px;
    
  &:after {
    
  }
  // TODO arrow
  &:before {
    
  }
  
  ${commentBodyStyles};
`;

interface ParentProps {
  comments: string|null;
}

export const TribunalAppealReportCommentBox: React.SFC<ParentProps> = (props) => {
  const { comments } = props;

  if (!comments) {
    return null;
  }

  return (
    <FlexContainer>
      <ChatBubble>
        <span dangerouslySetInnerHTML={{__html: comments}} />
      </ChatBubble>
    </FlexContainer>
  );
};
