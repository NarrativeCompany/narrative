import * as React from 'react';
import { SectionHeader } from '../SectionHeader';
import { FormattedMessage } from 'react-intl';
import { NicheTagRow } from './NicheTagRow';
import { NicheTag } from './NicheTag';
import { Niche } from '@narrative/shared';

interface ParentProps {
  title?: FormattedMessage.MessageDescriptor;
  onAddSelectedNiche: (niche: Niche) => void;
  suggestedNiches: Niche[];
}

export const SuggestedNiches: React.SFC<ParentProps> = (props) => {
  const { title, onAddSelectedNiche, suggestedNiches } = props;

  return (
    <React.Fragment>
      {title && <SectionHeader title={<FormattedMessage {...title}/>}/>}

      <NicheTagRow>
        {suggestedNiches.map(niche => (
          <NicheTag
            key={niche.oid}
            niche={niche}
            color="metallicBlue"
            iconType="plus"
            onClick={(n) => onAddSelectedNiche(n)}
          />
        ))}
      </NicheTagRow>
    </React.Fragment>
  );
};
