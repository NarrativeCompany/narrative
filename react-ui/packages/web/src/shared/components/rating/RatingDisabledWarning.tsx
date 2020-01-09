import * as React from 'react';
import { Modal } from 'antd';
import { RatingMessages } from '../../i18n/RatingMessages';
import { FormattedMessage } from 'react-intl';

export interface RatingDisabledWarningProps {
  visible?: boolean;
  dismiss: () => void;
}

export const RatingDisabledWarning: React.SFC<RatingDisabledWarningProps> = (props) => {
  const { visible, dismiss } = props;

  return (
    <Modal
      visible={visible}
      footer={null}
      onCancel={dismiss}
    >
      <FormattedMessage {...RatingMessages.RatingsDisabled}/>
    </Modal>
  );
};
