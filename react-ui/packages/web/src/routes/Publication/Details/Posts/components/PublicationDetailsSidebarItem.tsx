import * as React from 'react';
import { Publication } from '@narrative/shared';
import { BasicCard } from '../../../../../shared/components/card/BasicCard';

interface Props {
  publication: Publication;
}

export const PublicationDetailSidebarItem: React.SFC<Props> = (props) => {
  const { publication: {name, description} } = props;

  return (
    <BasicCard title={name}>
      {description}
    </BasicCard>
  );
};
