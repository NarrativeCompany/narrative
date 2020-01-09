import * as React from 'react';
import { ConfirmationModal, ConfirmationModalProps } from '../ConfirmationModal';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { Paragraph } from '../Paragraph';
import { Link } from '../Link';
import { FormattedMessage } from 'react-intl';

export interface DeleteForAupViolationConfirmationProps extends
  Pick<ConfirmationModalProps, 'visible' | 'onConfirmation' | 'dismiss' | 'processing'>
{
  entityName: FormattedMessage.MessageDescriptor;
  deleteButtonMessage: FormattedMessage.MessageDescriptor;
}

export const DeleteForAupViolationConfirmation: React.SFC<DeleteForAupViolationConfirmationProps> = (props) => {
  const { entityName, deleteButtonMessage, ...confirmationProps } = props;

  const entity = <FormattedMessage {...entityName}/>;
  const aupLink = <Link.Legal type="aup" />;

  return (
    <ConfirmationModal
      title={SharedComponentMessages.DeleteForViolation}
      btnText={deleteButtonMessage}
      btnProps={{ type: 'danger' }}
      linkText={SharedComponentMessages.Cancel}
      linkProps={{ onClick: props.dismiss }}
      {...confirmationProps}
    >
      <Paragraph marginBottom="large">
        <FormattedMessage {...SharedComponentMessages.DeleteForViolationDescription} values={{entity, aupLink}}/>
      </Paragraph>
    </ConfirmationModal>
  );
};
