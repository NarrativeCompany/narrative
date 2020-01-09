import * as React from 'react';
import { User, PublicationRole } from '@narrative/shared';
import { Modal } from 'antd';
import { EnhancedPublicationRole } from '../../../../../shared/enhancedEnums/publicationRole';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { ConfirmationModal } from '../../../../../shared/components/ConfirmationModal';
import { FormattedMessage } from 'react-intl';
import { MemberLink } from '../../../../../shared/components/user/MemberLink';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { SharedComponentMessages } from '../../../../../shared/i18n/SharedComponentMessages';

export interface RemovePowerUserModalProps {
  user?: User;
  role?: PublicationRole;
  // jw: while this is a derivable flag, I would rather not have to apply withExtractedCurrentUser here as well as other
  //     places. For ease of use I have split the loading of this modal into two functions, so the controller of these
  //     properties will be responsible for letting us know when it's for the current user.
  removingSelf?: boolean;
  processing?: boolean;
  removeConfirmed: () => void;
  close: () => void;
}

export const RemovePowerUserModal: React.SFC<RemovePowerUserModalProps> = (props) => {
  const { user, role } = props;

  // jw: we should always have both the user and role when the modal is visible, so if we don't then let's just output
  //     a stub modal so that we have something on the page for transition effects. This also allows us to know that
  //     the properties have values after this point and avoid unnecessary undefined checks.
  if (!user || !role) {
    return <Modal />;
  }

  // jw: let's extract the last couple of props we need now that we know we are rendering a modasl.
  const { removingSelf, processing, removeConfirmed, close } = props;

  const roleType = EnhancedPublicationRole.get(role);
  const rolePluralName = <FormattedMessage {...roleType.pluralName}/>;
  const roleNameWithArticle = <FormattedMessage {...roleType.nameWithArticle}/>;
  const userLink = <MemberLink user={user} hideBadge={true} targetBlank={true} />;

  return (
    <ConfirmationModal
      visible={true}
      title={PublicationDetailsMessages.RemoveFromPowerUsersModalTitle}
      titleValues={{rolePluralName}}
      processing={processing}
      onConfirmation={removeConfirmed}
      dismiss={close}
      btnText={removingSelf
        ? PublicationDetailsMessages.RemoveSelfButtonText
        : PublicationDetailsMessages.RemoveUserButtonText
      }
      btnProps={{ type: 'primary' }}
      linkText={SharedComponentMessages.Cancel}
      linkProps={{ onClick: close }}
    >
      <Paragraph marginBottom="large">
        {removingSelf
          ? <FormattedMessage {...PublicationDetailsMessages.RemoveSelfQuestion} values={{roleNameWithArticle}} />
          : <FormattedMessage
              {...PublicationDetailsMessages.RemoveUserQuestion}
              values={{userLink, roleNameWithArticle}}
            />
        }
      </Paragraph>
    </ConfirmationModal>
  );
};
