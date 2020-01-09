import * as React from 'react';
import { branch, compose, withProps } from 'recompose';
import { Section } from '../../../../../shared/components/Section';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { WithPublicationDetailsContextProps } from '../../../components/PublicationDetailsContext';
import {
  User,
  WithPublicationPowerUsersProps,
  withState,
  WithStateProps
} from '@narrative/shared';
import { PowerUserRow } from './PowerUserRow';
import { Link } from '../../../../../shared/components/Link';
import {
  WithCurrentUserProps,
  withExtractedCurrentUser
} from '../../../../../shared/containers/withExtractedCurrentUser';
import { ChangePublicationOwnerModal, ChangePublicationOwnerModalProps } from './ChangePublicationOwnerModal';

type ParentProps = Pick<WithPublicationDetailsContextProps, 'publicationDetail'> &
  Pick<WithPublicationPowerUsersProps, 'publicationPowerUsers'> & {
  owner: User;
};

interface PotentialOwnersProps {
  potentialOwners?: User[];
}

interface State {
  changeOwnerModalVisible?: boolean;
}

interface Props extends ParentProps {
  openChangeOwnerModal?: () => void;
  changeOwnerModalProps?: ChangePublicationOwnerModalProps;
}

const PowerUsersOwnerSectionComponent: React.SFC<Props> = (props) => {
  const { openChangeOwnerModal, owner, changeOwnerModalProps } = props;

  let tool: React.ReactNode | undefined;
  if (openChangeOwnerModal) {
    tool = (
      <Link.Anchor onClick={openChangeOwnerModal}>
        <FormattedMessage {...PublicationDetailsMessages.ChangeOwnerLinkText}/>
      </Link.Anchor>
    );
  }

  return (
    <Section
      title={<FormattedMessage {...PublicationDetailsMessages.OwnerTitle} />}
      description={<FormattedMessage {...PublicationDetailsMessages.OwnerDescription} />}
    >
      <PowerUserRow user={owner} tool={tool} />

      {changeOwnerModalProps && <ChangePublicationOwnerModal {...changeOwnerModalProps}/>}
    </Section>
  );
};

type HandlerProps = WithStateProps<State> &
  PotentialOwnersProps &
  ParentProps;

export const PowerUsersOwnerSection = compose(
  withExtractedCurrentUser,
  withProps<PotentialOwnersProps, ParentProps & WithCurrentUserProps>(
    (props: ParentProps & WithCurrentUserProps): PotentialOwnersProps => {
      const { currentUser, owner, publicationPowerUsers: { admins } } = props;

      if (currentUser.oid !== owner.oid) {
        return {};
      }

      const potentialOwners = admins.filter(admin => admin.oid !== owner.oid);

      return { potentialOwners };
    }
  ),
  // jw: we only need to add all of the heavier HOCs for changing the owner if there is an option to change the owner
  branch<PotentialOwnersProps>(props => !!props.potentialOwners,
    compose(
      withState<State>({}),
      withProps<Pick<Props, 'openChangeOwnerModal' | 'changeOwnerModalProps'>, HandlerProps>(
        (props: HandlerProps): Pick<Props, 'openChangeOwnerModal' | 'changeOwnerModalProps'> => {
          const {
            setState,
            potentialOwners,
            state: { changeOwnerModalVisible: visible },
            publicationDetail: { publication }
          } = props;

          if (!potentialOwners) {
            // todo:error-handling: Considering the branch above we should always have a potentialOwners array here.
            return {};
          }

          const openChangeOwnerModal = () => setState(ss => ({...ss, changeOwnerModalVisible: true}));

          const changeOwnerModalProps: ChangePublicationOwnerModalProps = {
            visible,
            publication,
            potentialOwners,
            close: () => setState(ss => ({...ss, changeOwnerModalVisible: undefined}))
          };

          return { openChangeOwnerModal, changeOwnerModalProps };
        }
      )
    )
  )
)(PowerUsersOwnerSectionComponent) as React.ComponentClass<ParentProps>;
