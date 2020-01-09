import * as React from 'react';
import { FeaturePostModalsProps } from './FeaturePostModals';
import { Modal } from 'antd';
import { PublicationDetailsMessages } from '../../i18n/PublicationDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { Paragraph } from '../Paragraph';

export const FeaturePostTitleImageWarningModal: React.SFC<FeaturePostModalsProps> = (props) => {
  const { post, featurePostHandler, unfeaturePostHandler, closeModalHandler } = props;

  return (
    <Modal
      // jw: visible if we have a post, and we do not have either handler
      visible={!!post && !featurePostHandler && !unfeaturePostHandler}
      title={<FormattedMessage {...PublicationDetailsMessages.FeaturePostTitleImageWarningTitle} />}
      footer={null}
      destroyOnClose={true}
      onCancel={closeModalHandler}
    >
      <Paragraph size="small">
        <FormattedMessage {...PublicationDetailsMessages.FeaturePostTitleImageWarningDescription} />
      </Paragraph>
    </Modal>
  );
};
