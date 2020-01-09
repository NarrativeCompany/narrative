import * as React from 'react';
import { Publication } from '@narrative/shared';
import { compose, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { PublicationDetailsMessages } from '../../../shared/i18n/PublicationDetailsMessages';
import { Modal } from 'antd';
import { ExpiredPublicationError } from './ExpiredPublicationError';
import { Heading } from '../../../shared/components/Heading';
import { Paragraph } from '../../../shared/components/Paragraph';

// jw: this is the data we are trying to parse out of the error
interface ExpiredPublicationErrorDetails {
  publication: Publication;
  deletionDatetime: string;
  owner: boolean;
}

function isExpiredPublicationErrorDetails(unknown: {}): unknown is ExpiredPublicationErrorDetails {
  if (!unknown) {
    return false;
  }

  // tslint:disable-next-line no-string-literal
  return !(!unknown['publication'] || !unknown['deletionDatetime'] || !unknown['owner']);
}

interface ParentProps {
  visible?: boolean;
  close: () => void;
  // jw: the details needed for rendering the proper error are encoded into a string on the ErrorState, so we need to
  //     get that and try to make sense of it.
  errorDetails?: string | null;
}

interface Props extends Pick<ParentProps, 'visible' | 'close'> {
  details?: ExpiredPublicationErrorDetails;
}

const ExpiredPublicationErrorModalComponent: React.SFC<Props> = (props) => {
  const { details, visible, close } = props;

  // jw: if we were unable to parse details then let's just give a generic publication expiration message.
  if (!details) {
    return (
      <Modal
        visible={visible}
        onCancel={close}
        footer={null}
        destroyOnClose={true}
      >
        <Heading size={2}>
          <FormattedMessage {...PublicationDetailsMessages.CurrentlyUnavailable} />
        </Heading>

        <Paragraph>
          <FormattedMessage {...PublicationDetailsMessages.PublicationExpiredGenericMessage} />
        </Paragraph>
      </Modal>
    );
  }

  return (
    <Modal
      visible={visible}
      onCancel={close}
      footer={null}
      destroyOnClose={true}
    >
      <ExpiredPublicationError {...details}/>
    </Modal>
  );
};

export const ExpiredPublicationErrorModal = compose(
  withProps<Pick<Props, 'details'>, ParentProps>((props: ParentProps): Pick<Props, 'details'> => {
    const { errorDetails } = props;

    if (errorDetails) {
      const details = JSON.parse(errorDetails);
      if (isExpiredPublicationErrorDetails(details)) {
        return { details };
      }
    }

    return {};
  })
)(ExpiredPublicationErrorModalComponent) as React.ComponentClass<ParentProps>;
