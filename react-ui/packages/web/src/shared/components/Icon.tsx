import * as React from 'react';
import { Omit } from 'recompose';
import { Icon as AntIcon } from 'antd';
import { IconProps as AntIconProps } from 'antd/lib/icon';
import { ShareSvg as share } from './svg/ShareSvg';
import { EllipsisSvg as ellipsis } from './svg/EllipsisSvg';
import { BookmarkSvg as bookmark } from './svg/BookmarkSvg';
import { CoinkSvg as coin } from './svg/CoinSvg';
import { ThumbUpSvg } from './svg/ThumbUpSvg';
import { ThumbDownSvg } from './svg/ThumbDownSvg';
import { FeaturedPostSortSvg } from './svg/FeaturedPostSortSvg';
import { PostQualitySortSvg } from './svg/PostQualitySortSvg';
import { RecentPostSortSvg } from './svg/RecentPostSortSvg';
import { TrendingPostSortSvg } from './svg/TrendingPostSortSvg';
import { PinterestSvg } from './svg/PinterestSvg';
import { SnapchatSvg } from './svg/SnapchatSvg';

export const SVG_COMPONENTS = {
  share,
  ellipsis,
  bookmark,
  coin,
  'thumb-up': ThumbUpSvg,
  'thumb-down': ThumbDownSvg,
  'featured-post-sort': FeaturedPostSortSvg,
  'post-quality-sort': PostQualitySortSvg,
  'recent-post-sort': RecentPostSortSvg,
  'trending-post-sort': TrendingPostSortSvg,
  'snapchat': SnapchatSvg,
  'pinterest': PinterestSvg,
};

export type SvgComponent = keyof typeof SVG_COMPONENTS;

export interface IconProps extends Omit<AntIconProps, 'component'> {
  svgIcon?: SvgComponent;
}

export const Icon: React.SFC<IconProps> = (props) => {
  const { svgIcon, ...iconProps } = props;

  return (
    <AntIcon {...iconProps} component={svgIcon ? SVG_COMPONENTS[svgIcon] : undefined}/>
  );
};
