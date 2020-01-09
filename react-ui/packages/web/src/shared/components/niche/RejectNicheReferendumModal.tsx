import * as React from 'react';
import { Referendum } from '@narrative/shared';
import { Modal } from 'antd';
import { Heading } from '../Heading';
import { FormattedMessage } from 'react-intl';
import { NicheDetailsMessages } from '../../i18n/NicheDetailsMessages';
import { ReferendumRejectReasonForm } from '../referendum/ReferendumRejectReasonForm';

interface Props {
  referendum: Referendum;
  // tslint:disable-next-line no-any
  dismiss: () => any;
  visible: boolean;
}

export const RejectNicheReferendumModal: React.SFC<Props> = (props) => {
  const { referendum, dismiss, visible } = props;

  return (
    <Modal
      title={(
        <Heading size={3} noMargin={true}>
          <FormattedMessage {...NicheDetailsMessages.RejectModalTitle}/>
        </Heading>
      )}
      onCancel={dismiss}
      visible={visible}
      footer={null}
      width={600}
      destroyOnClose={true}
    >

      <ReferendumRejectReasonForm
        referendum={referendum}
        dismissForm={dismiss}
      />

    </Modal>
  );
};
