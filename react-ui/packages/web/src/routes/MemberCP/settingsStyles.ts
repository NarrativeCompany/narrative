import styled from '../../shared/styled';
import { FlexContainer, FlexContainerProps } from '../../shared/styled/shared/containers';
import { Heading, HeadingProps } from '../../shared/components/Heading';
import { Button } from 'antd';
import { Paragraph, ParagraphProps } from '../../shared/components/Paragraph';

// Let's talk about this file - not sure if this is necessary
// We can figure about what is reusable with profile settings as we move through the individual setting views

export const FormWrapper = styled(FlexContainer)<FlexContainerProps>`
  padding: 0 15px;
  & .ant-form-item {
    margin-bottom: 5px;
  }
  & .red .ant-checkbox + span {
    color: ${props => props.theme.primaryRed};
  }
  & .ant-checkbox-checked .ant-checkbox-inner {
    background-color: ${prop => prop.theme.primaryBlue};
    border-color: ${prop => prop.theme.primaryBlue};
  }
`;

export const SettingsGroup = styled.div`
  margin-bottom: 50px;
`;

export const SettingsGroupHeading = styled(Heading)`
  color: ${props => props.theme.textColorLight};
  font-weight: 300;
`;

export const DescriptionParagraph = styled<ParagraphProps>(Paragraph)`
  margin-top: 15px;
  margin-bottom: 25px;
`;

// tslint:disable-next-line no-any
export const SubmitButton = styled(Button as any)`
  &.ant-btn-primary {
    background-color: ${prop => prop.theme.primaryBlue};
    border-color: ${prop => prop.theme.primaryBlue};
    border-radius: 20px;
    text-transform: uppercase;
    padding: 0 40px;
  }
`;

export const FormWrapperDiv = styled.div`
  position: relative;
`;

export const Label = styled<HeadingProps>(Heading)`
  min-width: 175px;
  margin-top: 6px;
`;
