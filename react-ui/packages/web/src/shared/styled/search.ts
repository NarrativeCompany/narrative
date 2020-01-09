import styled from '.';
import { Form } from 'formik';
import { Button } from 'antd';
import { FlexContainer, FlexContainerProps } from './shared/containers';

export const SearchWrapper = styled(FlexContainer)<FlexContainerProps>`
  padding: 0 15px;
  height: 100%;
`;

export const SearchForm = styled(Form)`
  max-width: 80%;
  min-width: 80%;
`;

export const SearchHeader = styled.h1`
  font-size: 48px; 
  font-weight: 600;
`;

// tslint:disable-next-line no-any
export const SearchSubmitButton = styled(Button as any)`
  width: 100%;
`;
