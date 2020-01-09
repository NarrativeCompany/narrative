import * as React from 'react';
import { ConfirmationModal, ConfirmationModalProps } from '../ConfirmationModal';
import { Niche } from '@narrative/shared';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { FormattedMessage } from 'react-intl';
import { NicheLink } from '../niche/NicheLink';
import { Paragraph } from '../Paragraph';

export interface DeletePostFromNicheConfirmationProps extends
  Pick<ConfirmationModalProps, 'visible' | 'onConfirmation' | 'dismiss' | 'processing'>
{
  // jw: this seems odd, but until the user has chosen which niche they want to remove the post from, we will need to
  //     render without this.
  niche?: Niche;
}

export const DeletePostFromNicheConfirmation: React.SFC<DeletePostFromNicheConfirmationProps> = (props) => {
  const { niche, ...confirmationProps } = props;

  const dash = <React.Fragment>&mdash;</React.Fragment>;

  return (
    <ConfirmationModal
      title={SharedComponentMessages.RemovePostFromNiche}
      btnText={SharedComponentMessages.Remove}
      btnProps={{ type: 'danger' }}
      linkText={SharedComponentMessages.Cancel}
      linkProps={{ onClick: props.dismiss }}
      {...confirmationProps}
    >
      {niche &&
        <Paragraph color="light" marginBottom="large">
          <FormattedMessage
            {...SharedComponentMessages.RemovePostFromNicheDescription}
            values={{dash, nicheLink: <NicheLink niche={niche} target="_blank" />}}
          />
        </Paragraph>
      }
    </ConfirmationModal>
  );
};
