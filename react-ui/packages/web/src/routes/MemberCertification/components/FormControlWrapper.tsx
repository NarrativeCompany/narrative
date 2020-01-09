import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { Heading } from '../../../shared/components/Heading';
import styled from '../../../shared/styled';
import { Paragraph } from '../../../shared/components/Paragraph';
import { CustomIcon } from '../../../shared/components/CustomIcon';

const ContentWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 75px;
`;

const LabelWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 10px;
`;

const LabelText = styled<FlexContainerProps>(FlexContainer)`
  margin-right: auto;
`;

interface ParentProps {
  title: FormattedMessage.MessageDescriptor;
  description?: React.ReactNode;
  isComplete: boolean;
}

export const FormControlWrapper: React.SFC<ParentProps> = (props) => {
  const { title, description, isComplete, children } = props;

  return (
    <ContentWrapper column={true}>
      <LabelWrapper>
        <LabelText column={true}>
          <Heading size={6} uppercase={true}>
            <FormattedMessage {...title}/>
          </Heading>

          {description &&
          <Paragraph color="light">
            {description}
          </Paragraph>}
        </LabelText>

        {isComplete && <CustomIcon type="approve" size="sm" style={{ height: 20 }}/>}
      </LabelWrapper>

      {children}
    </ContentWrapper>
  );
};
