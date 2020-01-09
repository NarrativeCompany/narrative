import * as React from 'react';
import { branch, compose, renderComponent } from 'recompose';
import { PublicationDetailsConnect, WithPublicationDetailsContextProps } from './PublicationDetailsContext';
import { ExpiredPublicationErrorCard } from './ExpiredPublicationErrorCard';
import { EnhancedPublicationStatus } from '../../../shared/enhancedEnums/publicationStatus';

function displayError(props: WithPublicationDetailsContextProps, allowForOwnerWhenExpired?: boolean): boolean {
  const { currentUserRoles, publicationDetail: { publication: { status } } } = props;

  const statusType = EnhancedPublicationStatus.get(status);

  // jw: if the status is active then we can short out.
  if (!statusType.isExpired()) {
    return false;
  }

  // jw: if the publication is expired but we are allowing owners through then we can short out.
  if (allowForOwnerWhenExpired && currentUserRoles.owner) {
    return false;
  }

  // jw: at this point, we know they don't have access, so give the error message.
  return true;
}

export const withExpiredPublicationError = (allowForOwnerWhenExpired?: boolean) => {
  return compose(
    PublicationDetailsConnect,
    branch<WithPublicationDetailsContextProps>(p => displayError(p, allowForOwnerWhenExpired),
      renderComponent((props: WithPublicationDetailsContextProps) => {
        const { currentUserRoles, publicationDetail: { deletionDatetime, publication } } = props;

        if (!deletionDatetime) {
          // todo:error-handling: since we only get here if the status is anything other than active we should be
          //      guaranteed to have a deletionDatetime... How do we not?
          return null;
        }

        return (
          <ExpiredPublicationErrorCard
            publication={publication}
            deletionDatetime={deletionDatetime}
            owner={currentUserRoles.owner}
          />
        );
      })
    )
  );
};
