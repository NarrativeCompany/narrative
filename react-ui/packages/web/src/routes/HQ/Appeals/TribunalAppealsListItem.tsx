import * as React from 'react';
import { compose } from 'recompose';
import { TribunalIssue } from '@narrative/shared';
import { TribunalAppealCardHeader } from './TribunalAppealCardHeader';
import { TribunalAppealCardBody } from './TribunalAppealCardBody';
import { TribunalAppealCardFooter } from './TribunalAppealCardFooter';
import { TribunalAppealCard } from './TribunalAppealCard';

interface ParentProps {
  issue: TribunalIssue;
}

const TribunalAppealsListItemComponent: React.SFC<ParentProps> = (props) => {
  const { issue } = props;

  return (
    <TribunalAppealCard>
      <TribunalAppealCardHeader issue={issue}/>
      <TribunalAppealCardBody issue={issue}/>
      <TribunalAppealCardFooter issue={issue}/>
    </TribunalAppealCard>
  );
};

export const TribunalAppealsListItem = compose(
)(TribunalAppealsListItemComponent) as React.ComponentClass<ParentProps>;
