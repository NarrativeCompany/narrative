import * as React from 'react';
import { compose, withProps } from 'recompose';
import reviewIcon from '../../assets/icons/icon-review.svg';
import bidIcon from '../../assets/icons/icon-auction.svg';
import leadershipIcon from '../../assets/icons/icon-tribunal.svg';
import appealsIcon from '../../assets/icons/icon-appeals.svg';
import moderatorsIcon from '../../assets/icons/icon-moderators.svg';
import blogIcon from '../../assets/icons/icon-blog.svg';
import redditIcon from '../../assets/icons/icon-reddit.svg';
import telegramIcon from '../../assets/icons/icon-telegram.svg';
import editIcon from '../../assets/icons/icon-edit.svg';
import removeIcon from '../../assets/icons/icon-remove.svg';
import deleteIcon from '../../assets/icons/icon-delete.svg';
import auctionIcon from '../../assets/icons/icon-auction.svg';
import completeIcon from '../../assets/icons/icon-complete.svg';
import failureIcon from '../../assets/icons/icon-failure.svg';
import suggestedIcon from '../../assets/icons/icon-suggested.svg';
import electionIcon from '../../assets/icons/icon-election.svg';
import networkStatsIcon from '../../assets/icons/icon-network-stats.svg';
import smileIcon from '../../assets/icons/icon-smile.svg';
import frownIcon from '../../assets/icons/icon-frown.svg';
import approveIcon from '../../assets/icons/icon-approve.svg';
import idFrontIcon from '../../assets/icons/icon-id-front.svg';
import idBackIcon from '../../assets/icons/icon-id-back.svg';
import selfieIcon from '../../assets/icons/icon-selfie.svg';
import lowQualityIcon from '../../assets/icons/icon-low-quality.svg';
import highQualityIcon from '../../assets/icons/icon-high-quality.svg';
import crownIcon from '../../assets/icons/icon-crown.svg';
import magnifyingGlassIcon from '../../assets/icons/icon-mag-glass.svg';
import nrveAndUsdIcon from '../../assets/icons/icon-nrve-and-usd.svg';
import paymentIcon from '../../assets/icons/icon-payment.svg';
import launchIcon from '../../assets/icons/icon-launch.svg';
import FeaturedInPublicationIcon from '../../assets/icons/icon-featured-in-publication.svg';

/**
 * This component is deprecated.
 * If you need a custom icon add the svg as a react component in shared/components/svg
 * Then add the svg component to svgComponents object
 */

// TODO: remove this const during refactor after antd update (#997)
// jw: updated this object to act as a lookup of SVGs, which may change the nature of the `todo` above.
//     By having this act as a SVG lookup it minimizes the work necessary to add icons
export const IconTypes = {
  review: reviewIcon,
  bid: bidIcon,
  leadership: leadershipIcon,
  appeals: appealsIcon,
  moderators: moderatorsIcon,
  blog: blogIcon,
  reddit: redditIcon,
  telegram: telegramIcon,
  edit: editIcon,
  remove: removeIcon,
  delete: deleteIcon,
  auction: auctionIcon,
  complete: completeIcon,
  failure: failureIcon,
  suggested: suggestedIcon,
  election: electionIcon,
  'network-stats': networkStatsIcon,
  smile: smileIcon,
  frown: frownIcon,
  approve: approveIcon,
  idFront: idFrontIcon,
  idBack: idBackIcon,
  selfie: selfieIcon,
  lowQuality: lowQualityIcon,
  highQuality: highQualityIcon,
  crown: crownIcon,
  'magnifying-glass': magnifyingGlassIcon,
  'nrve-and-usd': nrveAndUsdIcon,
  payment: paymentIcon,
  launch: launchIcon,
  'featured-in-publication': FeaturedInPublicationIcon,
};

export type IconType = keyof typeof IconTypes;
export type IconSize = 'sm' | 'md' | 'lg' | number;

interface WithProps {
  icon: string;
  iconSize: string;
}

export interface CustomIconProps {
  type: IconType;
  size?: IconSize;
  style?: React.CSSProperties;
}

type Props =
  CustomIconProps &
  WithProps;

const CustomIconComponent: React.SFC<Props> = (props) => {
  // jw: we need to capture and spread the remaining properties onto the img below, otherwise styled wrappers will
  //     not be able to apply their styling to these Components.
  const { icon, size, iconSize, type, style, ...rest } = props;

  return (
    <img src={icon} alt={type} style={{width: iconSize, height: iconSize, ...style}} {...rest} />
  );
};

function getSVGForIcon (type: IconType): string {
  const customIconSvg = IconTypes[type];

  if (!customIconSvg) {
    // todo:error-handling: this should log an error with the server, and perhaps use a default placeholder so the user
    //      is not interrupted?
    throw new Error('getSVGForIcon: no icon type match');
  }

  return customIconSvg;
}

export function getSizeForIcon (size?: IconSize): string {
  const defaultSize = '40px';

  if (!size) {
    return defaultSize;
  }

  if (typeof size === 'number') {
    return `${size}px`;
  }

  switch (size) {
    case 'sm':
      return '24px';
    case 'md':
      return '55px';
    case 'lg':
      return '70px';
    default:
      return defaultSize;
  }
}

export const CustomIcon = compose(
  withProps((props: CustomIconProps) => {
    const { type, size } = props;
    const icon = getSVGForIcon(type);
    const iconSize = getSizeForIcon(size);

    return { icon, iconSize };
  })
)(CustomIconComponent) as React.ComponentClass<CustomIconProps>;
