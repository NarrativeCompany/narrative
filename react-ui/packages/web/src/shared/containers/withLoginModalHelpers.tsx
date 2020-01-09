import { compose, withProps } from 'recompose';
import { ModalConnect, ModalName, ModalStoreProps } from '../stores/ModalStore';
import { getSecuredLinkProps, LinkSecurerFunction, SecurableLinkProps } from '../components/Link';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from './withExtractedCurrentUser';

export interface WithLoginLinkSecurer {
  openLoginModal?: () => void;
  loginLinkSecurer?: LinkSecurerFunction;
}

// jw: at some point we may need something like the permissionFunctionSecurer from withPermissionsModalHelpers.

export type WithLoginModalHelpersProps = WithLoginLinkSecurer;

export const withLoginModalHelpers = compose(
  ModalConnect(ModalName.login),
  withExtractedCurrentUser,
  withProps((props: WithExtractedCurrentUserProps & ModalStoreProps) => {
    if (props.currentUser) {
      return null;
    }

    // jw: first, let's create a utility function to load the login modal since this will be repeated.
    const { modalStoreActions: { updateModalVisibility } } = props;
    const openLoginModal = () => updateModalVisibility(ModalName.login);

    // jw: let's create the link securer
    const loginLinkSecurer = (linkProps: SecurableLinkProps): SecurableLinkProps => {
      // jw: use this utility method to make sure that required attributes are present.
      return getSecuredLinkProps(linkProps, openLoginModal);
    };

    return { openLoginModal, loginLinkSecurer };
  })
);
