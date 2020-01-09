import * as React from 'react';
import { Post } from '@narrative/shared';
import { Tooltip } from 'antd';
import { FormattedMessage } from 'react-intl';
import { CustomIcon, IconSize } from '../CustomIcon';
import { PublicationDetailsMessages } from '../../i18n/PublicationDetailsMessages';

interface Props {
  post: Post;
  size?: IconSize;
  style?: React.CSSProperties;
}

export const FeaturedInPublicationIcon: React.SFC<Props> = (props) => {
  const { featuredInPublication } = props.post;

  // jw: if the post is not featured in its publication then short out.
  if (!featuredInPublication) {
    return null;
  }

  const { size, style } = props;

  const iconSize = size || 'sm';

  return (
    <Tooltip title={<FormattedMessage {...PublicationDetailsMessages.Featured}/>}>
      <CustomIcon size={iconSize} type="featured-in-publication" style={style}/>
    </Tooltip>
  );
};
