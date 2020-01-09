import { DialogHeader } from '../DialogHeader';
import { FormattedMessage } from 'react-intl';
import { DeleteAccountMessages } from '../../../../shared/i18n/DeleteAccountMessages';
import * as React from 'react';
import { AuthHeader } from '../../../../shared/styled/shared/auth';
import { Button } from '../../../../shared/components/Button';
import { FlexContainer } from '../../../../shared/styled/shared/containers';

interface ParentProps {
  // tslint:disable-next-line no-any
  handleDismiss: () => any;
}

export const DeleteAccountSuccessPanel: React.SFC<ParentProps> = (props) => {
  const {handleDismiss} = props;

  return (
    <React.Fragment>

      <DialogHeader
        methodError={null}
        title={
          <AuthHeader style={{color: 'RED', fontWeight: 'lighter'}}>
            <FormattedMessage {...DeleteAccountMessages.AccountDeleted}/>
          </AuthHeader>
        }
        description=""
      />

      <FlexContainer
        column={true}
        centerAll={true}>
        <Button
          onClick={handleDismiss}
          htmlType="button"
          size="default"
          type="primary"
          style={{width: 200, marginTop: 75}}
        >
          <FormattedMessage {...DeleteAccountMessages.Goodbye}/>
        </Button>
      </FlexContainer>

    </React.Fragment>
  );
};
