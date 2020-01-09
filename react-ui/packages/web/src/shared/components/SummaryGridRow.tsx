import * as React from 'react';
import { FlexContainer } from '../styled/shared/containers';
import { Paragraph, ParagraphProps } from './Paragraph';
import styled from '../styled';

interface Props {
  title: string | React.ReactNode;
}

const SummaryGridRowContainer = styled(FlexContainer)`
  padding: 10px;
  
  @media screen and (max-width: 539px) {
    flex-direction: column;
  }
`;

const TitleContainer = styled<ParagraphProps>(Paragraph)`
  min-width: 200px;
  font-weight: 600;
`;

export const SummaryGridRow: React.SFC<Props> = (props) => {
  const { title } = props;

  return (
    <SummaryGridRowContainer>
      <TitleContainer uppercase={true} color="dark" size="small">
        {title}
      </TitleContainer>
      <div>
        {props.children}
      </div>
    </SummaryGridRowContainer>
  );
};
