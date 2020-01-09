import * as React from 'react';
import { Form as FormikForm } from 'formik';
import { Button } from '../../../shared/components/Button';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { Heading } from '../../../shared/components/Heading';
import { Paragraph } from '../../../shared/components/Paragraph';
import { IconProps, ThemeType } from 'antd/lib/icon';
import { Icon } from 'antd';
import { ButtonType } from 'antd/lib/button';
import { FormattedMessage } from 'react-intl';
import styled from '../../../shared/styled/index';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';

const FormWrapper = styled<FlexContainerProps>(FlexContainer)`
  width: 100%;
  height: 100%;
`;

const Form = styled(FormikForm)`
  width: 100%;
  height: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
`;

const HeadingWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 5px;
  width: 100%;
`;

// tslint:disable-next-line no-any
const CloseButton =
  styled<IconProps & {_theme?: ThemeType}>(({theme, _theme, ...rest}) => <Icon theme={_theme} {...rest}/>)`
    position: absolute;
    top: 10px;
    right: 10px;
    font-size: 18px;
    
    &:hover {
      color: ${props => props.theme.primaryRed};
    }
  `;

const ButtonWrapper = styled.div`
  width: 150px;
  margin: auto 0 5px;
`;

const FormBottomWrapper = styled<FlexContainerProps>(FlexContainer)`
  width: 100%;
  margin-top: 20px;
`;

// tslint:disable no-any
interface ParentProps {
  title: string | React.ReactNode;
  subTitle?: string | React.ReactNode;
  btnText: string | React.ReactNode;
  btnType: ButtonType;
  toggleCard?: () => any;
  children: React.ReactNode;
}
// tslint:enable no-any

export const NicheCardForm: React.SFC<ParentProps> = (props) => {
  const { title, subTitle, btnText, btnType, toggleCard, children } = props;

  return (
    <FormWrapper column={true}>
      <CloseButton type="close" onClick={toggleCard}/>

      <HeadingWrapper column={true}>
        <Heading size={4} weight={600}>{title}</Heading>
        {subTitle && <Paragraph color="light">{subTitle}</Paragraph>}
      </HeadingWrapper>

      <Form>
        {children}

        <FormBottomWrapper justifyContent="space-between" alignItems="center">
          <span onClick={toggleCard} style={{cursor: 'pointer'}}>
            <FormattedMessage {...SharedComponentMessages.Cancel}/>
          </span>
          <ButtonWrapper>
            <Button type={btnType} block={true}>{btnText}</Button>
          </ButtonWrapper>
        </FormBottomWrapper>
      </Form>
    </FormWrapper>
  );
};
