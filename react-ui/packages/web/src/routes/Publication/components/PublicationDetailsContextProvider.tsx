import * as React from 'react';
import { User } from '@narrative/shared';
import { PublicationDetailsContext, WithPublicationDetailsContextProps } from './PublicationDetailsContext';
import { getPublicationRoleBooleans } from '../../../shared/utils/publicationRoleUtils';

type ParentProps = Pick<WithPublicationDetailsContextProps, 'publicationDetail' | 'refetchPublicationDetail'> & {
  currentUser: User;
};

export const PublicationDetailsContextProvider: React.SFC<ParentProps> = (props) => {
  const { children, currentUser, publicationDetail, refetchPublicationDetail } = props;
  const currentUserRoles = getPublicationRoleBooleans(publicationDetail, currentUser);
  return (
    <PublicationDetailsContext.Provider value={{publicationDetail, currentUserRoles, refetchPublicationDetail}}>
      {children}
    </PublicationDetailsContext.Provider>
  );
};
