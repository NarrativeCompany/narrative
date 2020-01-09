import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import { Card } from '../../../shared/components/Card';
import { PostDetailMessages } from '../../../shared/i18n/PostDetailMessages';
import { Niche } from '@narrative/shared';
import { Link } from '../../../shared/components/Link';

interface ParentProps {
  niches: Niche[];
}

export const PostNiches: React.SFC<ParentProps> = (props) => {
  const { niches } = props;

  return (
    <React.Fragment>
      <SectionHeader
        title={<FormattedMessage {...PostDetailMessages.PostNichesSectionTitle}/>}
        extra={<Link.About type="nicheAbout" />}
      />

      {niches.map((niche: Niche) => (
        <Card.Channel
          key={niche.oid}
          channel={niche}
          noBoxShadow={true}
          style={{ marginBottom: 20 }}
        />
      ))}
    </React.Fragment>
  );
};
