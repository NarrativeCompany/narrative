import * as React from 'react';
import { Alert } from 'antd';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import { compose } from 'recompose';
import { FlexContainer } from '../styled/shared/containers';
import { MethodError } from '@narrative/shared';
import { Paragraph } from './Paragraph';

type ParentProps = MethodError;

type Props =
  ParentProps &
  InjectedIntlProps;

const FormMethodErrorComponent: React.SFC<Props> = (props) => {
  const {methodError} = props;

  if (methodError) {
    const description = methodError.map((message, i) => <Paragraph key={i}>{message}</Paragraph>);

    return (
      <Alert
        type="error"
        showIcon={true}
        message={
          <React.Fragment>
            <FlexContainer>
              <h4><FormattedMessage {...SharedComponentMessages.FormErrorTitle}/></h4>
            </FlexContainer>
            <FlexContainer>
              <h5><FormattedMessage {...SharedComponentMessages.FormErrorMessage}/></h5>
            </FlexContainer>
          </React.Fragment>
        }
        description={description}
        style={{ marginBottom: 15 }}
        closable={true}
      />
    );
  } else {
    return null;
  }
};

export const FormMethodError = compose(
  injectIntl
)(FormMethodErrorComponent) as React.ComponentClass<ParentProps>;
