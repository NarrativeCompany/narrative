import { withProps, compose } from 'recompose';
import { withExtractedAuthState, WithExtractedAuthStateProps } from './withExtractedAuthState';
import { ApolloQueryResult } from 'apollo-client';
import {
  User,
  UserAgeStatus,
  CurrentUser_formatPreferences,
  GlobalPermissions,
  withCurrentUser,
  WithCurrentUserProps as WithCurrentUserQueryProps
} from '@narrative/shared';

export interface WithExtractedCurrentUserProps extends WithExtractedAuthStateProps {
  currentUserLoading?: boolean;
  currentUser?: User;
  currentUserAgeStatus?: UserAgeStatus;
  currentUserPersonalJournalOid: string;
  currentUserFormatPreferences?: CurrentUser_formatPreferences;
  currentUserGlobalPermissions?: GlobalPermissions;
  // tslint:disable-next-line: no-any
  refetchCurrentUser?: () => Promise<ApolloQueryResult<any>>;
}

// jw: there are places where we branch the stack once we confirm that we have a current user, for those cases, it is
//     nice not having to do existence checks every time we need to reference one of these.
export interface WithCurrentUserProps extends WithExtractedAuthStateProps {
  currentUser: User;
  currentUserAgeStatus: UserAgeStatus;
  currentUserFormatPreferences: CurrentUser_formatPreferences;
  currentUserGlobalPermissions: GlobalPermissions;
  // tslint:disable-next-line: no-any
  refetchCurrentUser: () => Promise<ApolloQueryResult<any>>;
}

type Props =
  WithCurrentUserQueryProps &
  WithExtractedAuthStateProps;

export const withExtractedCurrentUser = compose(
  withExtractedAuthState,
  withCurrentUser,
  withProps((props: Props) => {
    const { currentUserData } = props;
    const currentUserLoading =
      currentUserData &&
      currentUserData.loading;
    const currentUserObj =
      currentUserData &&
      currentUserData.getCurrentUser;
    const refetchCurrentUser =
      currentUserData &&
      currentUserData.refetch;
    const currentUser =
      currentUserObj &&
      currentUserObj.user;
    const currentUserAgeStatus =
      currentUserObj &&
      currentUserObj.userAgeStatus;
    const currentUserPersonalJournalOid =
      currentUserObj &&
      currentUserObj.personalJournalOid;
    const currentUserFormatPreferences =
      currentUserObj &&
      currentUserObj.formatPreferences;
    const currentUserGlobalPermissions =
      currentUserObj &&
      currentUserObj.globalPermissions;

    return {
      currentUserLoading,
      currentUser,
      currentUserPersonalJournalOid,
      currentUserAgeStatus,
      currentUserFormatPreferences,
      currentUserGlobalPermissions,
      refetchCurrentUser
    };
  })
);
