import styled from '../index';
import { FlexContainer, FlexContainerProps } from './containers';
import { Form } from 'formik';

export const AuthWrapper = styled<FlexContainerProps>(FlexContainer)`
  padding: 0 15px;
  height: 100%;
  width: 100%;
`;

export const AuthForm = styled(Form)`
  max-width: 400px;
  width: 100%;
`;

export const AuthHeader = styled.h1`
  font-size: 30px; 
  font-weight: 600;
  justify: center;
`;

export const RememberMeWrapper = styled<FlexContainerProps>(FlexContainer)`
  .ant-form-item {
    white-space: nowrap;
  }
  margin-bottom: 24px;
`;

export const AuthLinkWrapper = styled<FlexContainerProps>(FlexContainer)`
  > p {
    margin-right: 12px;
  }
`;
