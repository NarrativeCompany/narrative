import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { Heading } from '../../../shared/components/Heading';
import { CustomIcon } from '../../../shared/components/CustomIcon';
import styled from '../../../shared/styled';

const CertificationStepWrapper =
  styled<FlexContainerProps & {isComplete?: boolean}>(({isComplete, ...rest}) => <FlexContainer {...rest}/>)`
    margin-bottom: ${p => p.isComplete ? 0 : 50}px;
    
    &:last-child {
      margin-bottom: 0;
    }
  `;

interface ParentProps {
  title: React.ReactNode;
  description?: React.ReactNode;
  isComplete?: boolean;
}

export const MemberCertificationStep: React.SFC<ParentProps> = (props) => {
  const { title, description, isComplete, children } = props;

  return (
    <CertificationStepWrapper column={true} isComplete={isComplete}>
      <FlexContainer column={true} style={{ marginBottom: 25 }}>
        <FlexContainer alignItems="center" style={{ marginBottom: 12 }}>
          <Heading size={5} noMargin={true}>
            {title}
          </Heading>

          {isComplete &&
          <CustomIcon type="approve" size={16} style={{ marginLeft: 10 }}/>}
        </FlexContainer>

        {description}
      </FlexContainer>

      {children}
    </CertificationStepWrapper>
  );
};
