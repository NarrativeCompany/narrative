import * as React from 'react';
import { Button } from '../../../shared/components/Button';
import { Link } from '../../../shared/components/Link';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import styled from '../../../shared/styled';
import { ButtonHTMLType } from 'antd/lib/button/button';

interface ParentProps {
  continueLabel: React.ReactNode;
  continueEnabled?: boolean | true;
  continueHTMLType?: ButtonHTMLType | 'button';
  cancelLabel: React.ReactNode;
  stackVertical?: boolean | true;
  // tslint:disable-next-line no-any
  handleContinue?: () => any;
  // tslint:disable-next-line no-any
  handleCancel: () => any;
  continueStyle?: React.CSSProperties;
  cancelStyle?: React.CSSProperties;
  loading?: boolean;
}

const Anchor = styled(Link.Anchor)`
  margin-top: 10px;
  margin-bottom: 15px;
  color: ${props => props.theme.textColorLight};

`;

const ButtonContainer =
  styled<FlexContainerProps & {flexDirection: string}>(({flexDirection, ...rest}) => <FlexContainer {...rest}/>)`
    ${props => props.flexDirection && `
    flex-direction: ${props.flexDirection};
    `}
    margin-top: 25px;
    margin-bottom: 15px;
  `;

export const ContinueCancelButtons: React.SFC<ParentProps> = (props) => {
  const {
    handleContinue,
    handleCancel,
    continueEnabled,
    stackVertical,
    continueLabel,
    cancelLabel,
    cancelStyle,
    continueStyle,
    continueHTMLType,
    loading
  } = props;

  return (
    <React.Fragment>

      <ButtonContainer
        column={true}
        centerAll={stackVertical}
        alignItems={stackVertical ? 'center' : 'flex-end'}
        justifyContent={stackVertical ? 'center' : 'space-between'}
        flexDirection={stackVertical ? 'column' : 'row-reverse'}
      >

        <Button
          onClick={handleContinue}
          htmlType={continueHTMLType}
          size="default"
          type="primary"
          disabled={!continueEnabled}
          style={continueStyle}
          loading={loading}
        >
          {continueLabel}
        </Button>

        <Anchor onClick={handleCancel} style={cancelStyle}>
          {cancelLabel}
        </Anchor>

      </ButtonContainer>

    </React.Fragment>
  );
};
