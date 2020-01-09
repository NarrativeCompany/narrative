import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { publicationPowerUserInvitationQuery } from '../graphql/publication/publicationPowerUserInvitationQuery';
import { PublicationRole, PublicationPowerUserInvitationQuery } from '../../types';
import { LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithPublicationPowerUserInvitationProps extends LoadingProps {
  // jw: for ease of consumption let's parse out the roles from the containing object. There is nothing else in it of
  //     value so this is probably best.
  invitedRoles: PublicationRole[];
}

export interface WithPublicationPowerUserInvitationParentProps {
  publicationOid: string;
}

const queryName = 'publicationPowerUserInvitationData';

type Props = NamedProps<
    {[queryName]: GraphqlQueryControls & PublicationPowerUserInvitationQuery},
    WithPublicationPowerUserInvitationParentProps
  >;

export const withPublicationPowerUserInvitation =
  graphql<
    WithPublicationPowerUserInvitationParentProps,
    PublicationPowerUserInvitationQuery,
    {},
    WithPublicationPowerUserInvitationProps
  >(publicationPowerUserInvitationQuery, {
    name: queryName,
    options: ({publicationOid}) => ({
      variables: {
        publicationOid
      }
    }),
    props: ({ publicationPowerUserInvitationData }: Props): WithPublicationPowerUserInvitationProps => {
      const { loading } = publicationPowerUserInvitationData;
      const invitedRoles = publicationPowerUserInvitationData.getPublicationPowerUserInvitation &&
        publicationPowerUserInvitationData.getPublicationPowerUserInvitation.invitedRoles || [];

      return { loading, invitedRoles };
    }
  });
