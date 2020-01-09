import { branch, compose, Omit, withHandlers, withProps } from 'recompose';
import { withState, WithStateProps } from '@narrative/shared';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from './withExtractedCurrentUser';
import { isPermissionGranted } from './withPermissionsModalController';
import { FormattedMessage } from 'react-intl';
import { DeleteForAupViolationConfirmationProps } from '../components/tribunal/DeleteForAupViolationConfirmation';

export interface WithOnObjectDeletedForAupViolationHandler {
  onObjectDeletedForAupViolation: () => void;
}

export interface WithDeleteObjectForAupViolationHandler {
  handleDeleteObjectForAupViolation: (objectOid: string) => void;
}

export type WithDeleteForAupViolationControllerParentHandlers =
  WithOnObjectDeletedForAupViolationHandler &
  WithDeleteObjectForAupViolationHandler;

type ParentHandlers = WithDeleteForAupViolationControllerParentHandlers;

interface State {
  deleteObjectOidForAup?: string;
  deletingObjectForAup?: boolean;
}

interface Handlers {
  openDeleteForAupConfirmation: (objectOid: string) => void;
  closeDeleteForAupConfirmation: () => void;
  deleteForAupConfirmed: () => void;
}

export interface WithOpenDeleteForAupViolationConfirmationHandler {
  openDeleteForAupViolationConfirmation?: (objectOid: string) => void;
}

type ProvidedDeleteAupConfirmationProps = Omit<DeleteForAupViolationConfirmationProps, 'deleteButtonMessage'>;

export interface WithDeleteForAupViolationControllerProps extends WithOpenDeleteForAupViolationConfirmationHandler {
  deleteForAupViolationConfirmationProps?: ProvidedDeleteAupConfirmationProps;
}

export const withDeleteForAupViolationController = function(entityName: FormattedMessage.MessageDescriptor) {
  return compose(
    withExtractedCurrentUser,
    // jw: only setup if the current user can participate in tribunal actions
    branch(({currentUserGlobalPermissions}: WithExtractedCurrentUserProps) =>
        isPermissionGranted('removeAupViolations', currentUserGlobalPermissions),
      compose(
        // jw: first, setup state.
        withState<State>({}),
        // jw: next: let's
        withHandlers({
          openDeleteForAupViolationConfirmation: (props: WithStateProps<State>) => (deleteObjectOidForAup: string) => {
            props.setState(ss => ({...ss, deleteObjectOidForAup}));
          },
          closeDeleteForAupConfirmation: (props: WithStateProps<State>) => () => {
            props.setState(ss => ({...ss, deleteObjectOidForAup: undefined}));
          },
          deleteForAupConfirmed: (props: WithStateProps<State> & ParentHandlers) => async () => {
            const {
              handleDeleteObjectForAupViolation,
              onObjectDeletedForAupViolation,
              setState,
              state: { deleteObjectOidForAup }
            } = props;

            if (!deleteObjectOidForAup) {
              // jw:todo:error-handling: Should always have a delteObjectOidForAup at this point, otherwise what was
              //         confirmed by the user?
              return;
            }

            setState(ss => ({...ss, deletingObjectForAup: true}));
            try {
              await handleDeleteObjectForAupViolation(deleteObjectOidForAup);

              // jw: now that the object is deleted, let's let the parent know
              if (onObjectDeletedForAupViolation) {
                onObjectDeletedForAupViolation();
              }

            } finally {
              setState(ss => ({...ss, deleteObjectOidForAup: undefined, deletingObjectForAup: undefined}));
            }
          }
        }),
        // jw: now that the handlers are all in place, let's go ahead and setup our exported properties
        withProps((props: WithStateProps<State> & Handlers) => {
          const {
            closeDeleteForAupConfirmation,
            deleteForAupConfirmed,
            state: { deleteObjectOidForAup, deletingObjectForAup }
          } = props;

          const deleteForAupViolationConfirmationProps: ProvidedDeleteAupConfirmationProps = {
            visible: !!deleteObjectOidForAup,
            processing: deletingObjectForAup,
            dismiss: closeDeleteForAupConfirmation,
            onConfirmation: deleteForAupConfirmed,
            entityName
          };

          return { deleteForAupViolationConfirmationProps };
        })
      )
    )
  );
};
