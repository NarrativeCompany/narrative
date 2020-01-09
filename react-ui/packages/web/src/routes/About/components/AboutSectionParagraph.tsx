import * as React from 'react';
import { FormattedMessage, MessageValue } from 'react-intl';
import { Paragraph } from '../../../shared/components/Paragraph';
import { Block } from '../../../shared/components/Block';
import { isIntlMessageDescriptor } from '../../../shared/utils/intlUtils';

interface Props {
  message?: string | FormattedMessage.MessageDescriptor;
  values?: {[key: string]: MessageValue | JSX.Element};
  style?: React.CSSProperties;
  children?: React.ReactNode;
  asBlock?: boolean;
}

export const AboutSectionParagraph: React.SFC<Props> = (props) => {
  const { message, values, style, children, asBlock } = props;

  let messageResolved;
  if (message) {
    if (isIntlMessageDescriptor(message)) {
      messageResolved = <FormattedMessage {...message} values={values} />;
    } else {
      messageResolved = message;
    }
  }

  if (asBlock) {
    return (
      <Block size="large" style={style}>
        {messageResolved}
        {children}
      </Block>
    );
  }

  return (
    <Paragraph size="large" style={style}>
      {messageResolved}
      {children}
    </Paragraph>
  );
};
