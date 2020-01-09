import * as React from 'react';
import { branch, compose, renderNothing, withProps } from 'recompose';
import {
  withPublicationUserAssociations,
  WithPublicationUserAssociationsProps
} from '@narrative/shared';
import { SidebarMessages } from '../../i18n/SidebarMessages';
import { FormattedMessage } from 'react-intl';
import { WithCurrentUserProps, withExtractedCurrentUser } from '../../containers/withExtractedCurrentUser';
import { LoadingProps } from '../../utils/withLoadingPlaceholder';
import { SectionHeader } from '../SectionHeader';
import { Block } from '../Block';
import { PublicationLink } from '../publication/PublicationLink';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import styled from 'styled-components';
import { PublicationAvatar } from '../publication/PublicationAvatar';

type Props = Pick<WithPublicationUserAssociationsProps, 'associations'>;

const PublicationWrapper = styled<FlexContainerProps>(FlexContainer)`
  justify-content: space-between;
  align-items: center;
  margin: 0 5px 5px;
`;

const YourPublicationsSidebarItemComponent: React.SFC<Props> = (props) => {
  const { associations } = props;

  return (
    <Block style={{marginBottom: 30}}>
      <SectionHeader
        title={<FormattedMessage {...SidebarMessages.YourPublications}/>}
        description={<FormattedMessage {...SidebarMessages.YourPublicationsDescription}/>}
      />
      {associations.map(association => (
        <PublicationWrapper key={association.publication.oid}>
          <PublicationLink publication={association.publication}/>
          <PublicationAvatar publication={association.publication}/>
        </PublicationWrapper>
      ))}
    </Block>
  );
};

export const YourPublicationsSidebarItem = compose(
  withExtractedCurrentUser,
  // bl: by getting here we know that we have a current user, so let's use the property set that assumes that.
  withProps((props: WithCurrentUserProps) => {
    return { userOid: props.currentUser.oid };
  }),
  withPublicationUserAssociations,
  // bl: we want to render nothing when loading because if the user is not associated with any publications this widget
  // will not be rendered, so we do not want to render a placeholder and then just remove it.
  branch((props: LoadingProps) => props.loading,
    renderNothing
  ),
  // bl: similar to above, if we do not have any publication associations at this point, then just render nothing
  branch((props: Props) => !props.associations || !props.associations.length,
    renderNothing
  ),
)(YourPublicationsSidebarItemComponent) as React.ComponentClass<{}>;
