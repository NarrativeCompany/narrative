import * as React from 'react';
import { Publication, PublicationRole } from '@narrative/shared';
import { EnhancedPublicationRole } from '../../../../../shared/enhancedEnums/publicationRole';
import { FormattedMessage } from 'react-intl';
import { Card, CardProps } from '../../../../../shared/components/Card';
import { Text } from '../../../../../shared/components/Text';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { WebRoute } from '../../../../../shared/constants/routes';
import { Link } from '../../../../../shared/components/Link';
import styled from '../../../../../shared/styled';
import { generatePath } from 'react-router';

interface Props {
  publication: Publication;
  viewedByOwner: boolean;
  role: PublicationRole;
}

const StyledCard = styled<CardProps>(Card)`
  &.ant-card {
    margin-top: 15px;
    .ant-card-body {
      padding: 10px 15px;
    }
  }
`;

export const PowerUserRoleLimitReachedWarning: React.SFC<Props> = (props) => {
  const { publication, viewedByOwner, role } = props;
  const roleType = EnhancedPublicationRole.get(role);

  const roleName = <FormattedMessage {...roleType.name} />;

  if (!viewedByOwner) {
    return (
      <Card>
        <Text color="warning">
          <FormattedMessage {...PublicationDetailsMessages.RoleLimitReachedWarning} values={{roleName}}/>
        </Text>
      </Card>
    );
  }

  const id = publication.prettyUrlString;
  const rolePluralName = <FormattedMessage {...roleType.pluralName} />;
  const upgradePlanLink = (
    <Link to={generatePath(WebRoute.PublicationAccount, {id})}>
      <FormattedMessage {...PublicationDetailsMessages.RoleLimitReachedUpgradePlanLinkText}/>
    </Link>
  );

  return (
    <StyledCard>
      <Text color="warning">
        <FormattedMessage
          {...PublicationDetailsMessages.RoleLimitReachedOwnerWarning}
          values={{roleName, rolePluralName, upgradePlanLink}}/>
      </Text>
    </StyledCard>
  );
};
