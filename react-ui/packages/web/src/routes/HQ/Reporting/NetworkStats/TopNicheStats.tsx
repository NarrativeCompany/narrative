import * as React from 'react';
import { TopNiche } from '@narrative/shared';
import { Paragraph } from '../../../../shared/components/Paragraph';
import { Link } from '../../../../shared/components/Link';
import { LocalizedNumber } from '../../../../shared/components/LocalizedNumber';
import { getNicheUrlByValue } from '../../../../shared/utils/nicheUtils';
import { TextSize } from '../../../../shared/components/Text';

interface Props {
  index: number;
  topNiche: TopNiche;
  size?: TextSize;
}

export const TopNicheStats: React.SFC<Props> = (props) => {
  const { index, size, topNiche: { oid, name, prettyUrlString, totalPosts } } = props;

  const nicheUrl = getNicheUrlByValue(prettyUrlString, oid);

  return (
    <Paragraph color="light" size={size || 'small'}>
      {index + 1}.&nbsp;
      <Link.Anchor href={nicheUrl}>{name}</Link.Anchor>
      &nbsp;(<LocalizedNumber value={totalPosts} />)
    </Paragraph>
  );
};
