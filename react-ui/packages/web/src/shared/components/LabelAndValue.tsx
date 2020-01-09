import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../styled/shared/containers';
import { ParagraphProps, Paragraph } from './Paragraph';
import { FormattedMessage } from 'react-intl';
import styled from '../styled';

const LabelAndValueWrapper = styled<FlexContainerProps & {noMargin?: boolean}>(FlexContainer)`
  margin-bottom: ${props => props.noMargin ? 0 : '15px'};
`;

const StyledLabel = styled<ParagraphProps>(Paragraph)`
  text-transform: uppercase;
  font-weight: 600;
`;

// tslint:disable-next-line no-any
function isMessageDescriptor (label: LabelType): label is FormattedMessage.MessageDescriptor {
  return label ? label.hasOwnProperty('defaultMessage') : false;
}

type LabelType = React.ReactNode | FormattedMessage.MessageDescriptor;

interface LabelAndValueProps extends FlexContainerProps {
  label: LabelType;
  noMargin?: boolean;
  style?: React.CSSProperties;
}

export const LabelAndValue: React.SFC<LabelAndValueProps> = (props) => {
  const { label, children, noMargin, style, ...containerProps } = props;

  const Label = isMessageDescriptor(label) ?
    <StyledLabel color="dark" size="small"><FormattedMessage {...label}/></StyledLabel> :
    label;

  return (
    <LabelAndValueWrapper
      noMargin={noMargin}
      style={style}
      alignItems="center"
      justifyContent="space-between"
      {...containerProps}
    >
      {Label}

      {children}
    </LabelAndValueWrapper>
  );
};
