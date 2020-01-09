import * as React from 'react';
import { Modal } from 'antd';
import { RatingMessages } from '../../i18n/RatingMessages';
import { FormattedMessage } from 'react-intl';

export interface AuthorRateContentWarningProps {
  visible: boolean;
  dismiss: () => void;
  forComment?: boolean;
}

export const AuthorRateContentWarning: React.SFC<AuthorRateContentWarningProps> = (props) => {
  const { visible, dismiss, forComment } = props;

  const message = forComment
    ? RatingMessages.CannotRateOwnComment
    : RatingMessages.CannotRateOwnPost;

  return (
    <Modal
      visible={visible}
      footer={null}
      onCancel={dismiss}
    >
      <FormattedMessage {...message}/>
    </Modal>
  );
};
