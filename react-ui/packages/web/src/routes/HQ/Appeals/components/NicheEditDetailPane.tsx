import * as React from 'react';
import styled from '../../../../shared/styled';
import { FlexContainer, FlexContainerProps } from '../../../../shared/styled/shared/containers';
import { Heading } from '../../../../shared/components/Heading';
import { Paragraph } from '../../../../shared/components/Paragraph';

const VersionWrapper = styled<FlexContainerProps>(FlexContainer)`
  max-width: 365px;
  border: 1px solid;
  border-radius: 10px;
  justify-content: space-between;
  padding: 10px;
`;

const CurrentVersionWrapper = styled(VersionWrapper)`
  border-color: #E2E6EC;
  background-color: #FFFFFF;
`;

const NewVersionWrapper = styled(VersionWrapper)`
  border-color: #40a9ff;
  background-color: #F2FBFD;
`;

interface ParentProps {
  name: string|null;
  description: string|null;
  isNewVersion: boolean;
}

export const NicheEditDetailPane: React.SFC<ParentProps> = (props) => {
  const { name, description, isNewVersion } = props;

  if (isNewVersion) {
    return (
      <NewVersionWrapper column={true}>
        {name && <Heading size={4}>{name}</Heading>}
        {description && <Paragraph>{description}</Paragraph>}
      </NewVersionWrapper>
    );
  } else {
    return (
      <CurrentVersionWrapper column={true}>
        {name && <Heading size={4}>{name}</Heading>}
        {description && <Paragraph>{description}</Paragraph>}
      </CurrentVersionWrapper>
    );
  }
};
