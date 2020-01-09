import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import styled from '../../styled';

export const NicheTagRow = styled<FlexContainerProps>((props) =>
  <FlexContainer {...props} flexWrap="wrap"/>)`
    &:not(:last-child) {
      margin-bottom: 60px;
    }
  `;
