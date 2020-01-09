import * as React from 'react';
import { Modal } from 'antd';
import { FormattedMessage } from 'react-intl';
import { GlobalErrorModalMessages } from '../../shared/i18n/GlobalErrorModalMessages';

interface Props {
  visible?: boolean;
  handleDismiss: () => void;
  errorMessage: string | null;
}

export const ActivityRateLimitExceededModal: React.SFC<Props> = (props) => {
  const { visible, handleDismiss, errorMessage } = props;

  if (!errorMessage) {
    // todo:error-handling: All UserActivityRateLimits must specify a error message, so this should never happen. Report
    //      this to ther server so that we can address whatever caused this state.
    return null;
  }

  return (
    <Modal
      title={<FormattedMessage {...GlobalErrorModalMessages.ActivityRateExceededTitle} />}
      visible={visible}
      onCancel={handleDismiss}
      footer={null}
      destroyOnClose={true}
    >
      {errorMessage}
    </Modal>
  );
};
