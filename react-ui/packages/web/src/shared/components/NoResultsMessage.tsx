import * as React from 'react';
import { FormattedMessage, MessageValue } from 'react-intl';
import { Paragraph } from './Paragraph';

interface Props {
  message: FormattedMessage.MessageDescriptor;
  values?: {[key: string]: MessageValue | JSX.Element};
}

export const NoResultsMessage: React.SFC<Props> = (props) => {
  const { message, values } = props;

  return (
    <Paragraph>
      <FormattedMessage {...message} values={values} />
    </Paragraph>
  );
};
