import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { EnhancedPublicationPlanType, PublicationPlanTypeHelper } from 'src/shared/enhancedEnums/publicationPlanType';
import { PublicationRoleHelper } from '../../enhancedEnums/publicationRole';
import { LocalizedNumber } from '../LocalizedNumber';

interface Props {
  roleType: PublicationRoleHelper;
}

export const PublicationPlanTableRoleRow: React.SFC<Props> = (props) => {
  const { roleType } = props;

  return (
    <tr>
      <td>{<FormattedMessage {...roleType.pluralName}/>}</td>
      {EnhancedPublicationPlanType.enhancers.map((planType: PublicationPlanTypeHelper) => (
        <td key={`roleRow-${roleType.role}-${planType.plan}`}>
          <LocalizedNumber value={roleType.getPlanLimit(planType)} />
        </td>
      ))}
    </tr>
  );
};
