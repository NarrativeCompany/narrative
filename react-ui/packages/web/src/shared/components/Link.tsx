import * as React from 'react';
import { Link as RouterLink, LinkProps as RouterLinkProps } from 'react-router-dom';
import { Icon as AntIcon } from 'antd';
import { CustomIcon, IconSize, IconType, IconTypes } from './CustomIcon';
import { FormattedMessage } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import styled, { css } from '../../shared/styled';
import { CSSProperties } from 'react';
import { externalUrls } from '../constants/externalUrls';
import { WebRoute } from '../constants/routes';
import * as H from 'history';
import { MouseEvent } from 'react';

// jw: Let's define some code to make it easy to secure a link using a function.

// jw: picking the href and onClick events off of LinkProps, but making to optional
export type SecurableLinkProps = Pick<LinkProps, 'href' | 'onClick'> & {to?: H.LocationDescriptor};
export type LinkSecurerFunction = (linkProps: SecurableLinkProps) => SecurableLinkProps;

interface LinkSecurerProp {
  linkSecurer?: LinkSecurerFunction;
}

// jw: the point of this function is to give the linkSecurer the opportunity to secure the properties while passing the
//     rest through. If there is no securer than this should result in zero changes.
function secureProps(props: SecurableLinkProps & LinkSecurerProp): SecurableLinkProps {
  // jw: even undefined, the linkSecurer is still present and causes problems when it's passed down the dom. With that
  //     in mind, let's strip it out entirely, so that after this point it will not even be present in an undefined
  //     state... That should solve some problems.
  const { linkSecurer, ...linkProps } = props;

  if (!linkSecurer) {
    return linkProps;
  }

  // jw: note: with this implementation the linkSecurer will be stripped out once the resolution has taken place.
  const { to, href, onClick, ...rest } = linkProps;

  return {
    ...rest,
    ...linkSecurer({to, href, onClick})
  };
}

// jw: because to is required by the react router, we need to be sure to include something for it. With that in mind,
//     let's make a utility function to easy this process.
export function getSecuredLinkProps(props: SecurableLinkProps, handler: () => void): SecurableLinkProps {
  if (!handler) {
    // todo:error-handling: We should always be provided a handler here.
    return props;
  }

  // jw: let's provide a onClick handler that will prevent default behavior, and propagation.
  return {
    onClick: (event: MouseEvent): void => {
      event.preventDefault();
      event.stopPropagation();
      handler();
    }
  };
}

// Link component utils
const addColorPrimary = css`
  color: ${props => props.theme.primaryBlue}
`;

const addColorBold = css`
  color: ${props => props.theme.textColorDark};
`;

const addColorLight = css`
  color: ${props => props.theme.textColorLight};
`;

const addColorInherit = css`
  color: inherit;
`;

const addTextDecorationUnderline = css`
  text-decoration: underline !important;
`;

const addTextDecorationNone = css`
  text-decoration: none !important;
`;

export function getLinkFontSize(size?: AnchorSizeType) {
  if (typeof size === 'number') {
    return `${size}px`;
  }

  switch (size) {
    case 'small':
      return '12px';
    case 'normal':
      return '14px';
    case 'large':
      return '18px';
    case 'inherit':
    default:
      return 'inherit';
  }
}

function getLinkColor(color?: AnchorColorType) {
  switch (color) {
    case 'light':
      return addColorLight;
    case 'dark':
      return addColorBold;
    case 'inherit':
      return addColorInherit;
    default:
      return addColorPrimary;
  }
}

function getTextDecoration(textDecoration?: AnchorTextDecoration) {
  switch (textDecoration) {
    case 'underline':
      return addTextDecorationUnderline;
    case 'none':
      return addTextDecorationNone;
    default:
      return addTextDecorationNone;
  }
}

// TODO: this check shouldn't be necessary after CustomIcon component is refactored (#997)
function isCustomIcon (icon: string | IconType): icon is IconType {
  return IconTypes.hasOwnProperty(icon);
}

// Link component types
export type AnchorSizeType = 'small' | 'normal' | 'large' | 'inherit' | number;
export type AnchorColorType = 'light' | 'dark' | 'default' | 'inherit';
export type AnchorTextDecoration = 'underline' | 'none';
export type LegalLinkType = 'tos' | 'aup' | 'aupAcronym' | 'privacy';
export type TokenSaleLinkType = 'latoken' | 'switcheo' | 'bilaxy';
export type AboutLinkType = keyof typeof aboutMessageTypes;

export interface LinkStyleProps extends LinkSecurerProp {
  size?: AnchorSizeType;
  color?: AnchorColorType;
  noHoverEffect?: boolean;
  textDecoration?: AnchorTextDecoration;
  weight?: number;
  style?: CSSProperties;
  target?: string;
  noFollow?: boolean;
  className?: string;
}

export type AnchorProps =
  React.AnchorHTMLAttributes<HTMLAnchorElement> &
  LinkStyleProps;

export type LinkIconProps =
  AnchorProps &
  LinkStyleProps & {
    iconType: string | IconType;
    iconSize?: number | IconSize;
  };

export type LinkLegalProps =
  AnchorProps &
  LinkStyleProps & {
    type: LegalLinkType;
  };

export type LinkTokenSaleProps =
  AnchorProps &
  LinkStyleProps & {
  type: TokenSaleLinkType;
  linkText?: React.ReactNode;
};

export type LinkAboutProps =
  AnchorProps &
  LinkStyleProps & {
  type: AboutLinkType;
};

export type LinkProps =
  LinkStyleProps &
  RouterLinkProps;

// Link component style
const StyledLink =
  styled<{as: React.ComponentClass | 'a'} & LinkStyleProps>
  (({as: Component, size, color, textDecoration, weight, noHoverEffect, ...rest}) =>
  <Component {...rest}/>)<LinkProps | AnchorProps | LinkIconProps>`
    ${props => getLinkColor(props.color)};
    ${props => getTextDecoration(props.textDecoration)};
    font-size: ${props => getLinkFontSize(props.size)};
    font-weight: ${props => props.weight ? props.weight : 'inherit'};
    
    // jw: there is a root style that is setting a:hover blue, so even if we are not using the hover effect we need 
    //     to include this definition and make it important
    &:hover,
    &:active {
      color: ${p => p.noHoverEffect ? 'inherit' : p.theme.secondaryBlue} !important;
    }
    
    img {
      display: inherit;
    }
    
    // bl: ant avatars need to be display:block to prevent cutting off the top of the image. we need this
    // to override the display:inherit rule right above, which is otherwise resulting in display:inline-block
    // being used, which is where the cropping issue arises.
    .ant-avatar > img {
      display: block;
    }
  `;

// Link.Anchor definition
const Anchor: React.SFC<AnchorProps> = (props) => (
  <StyledLink as="a" {...secureProps(props)}>
    {props.children}
  </StyledLink>
);

// Link.Icon definition
const Icon: React.SFC<LinkIconProps> = (props) => {
  const { iconType, iconSize, ...rest } = props;

  const LinkIcon = isCustomIcon(iconType) ?
    <CustomIcon type={iconType} size={iconSize}/> :
    <AntIcon type={iconType} style={{fontSize: `${iconSize}px`}}/>;

  return <StyledLink as="a" {...secureProps(rest)}>{LinkIcon}</StyledLink>;
};

// Link.TOS (terms of service) definition
const legalMessageTypes = {
  tos: {
    text: SharedComponentMessages.TermsOfService,
    url: externalUrls.narrativeTermsOfService
  },
  aup: {
    text: SharedComponentMessages.AcceptableUsePolicy,
    url: externalUrls.narrativeAup
  },
  aupAcronym: {
    text: SharedComponentMessages.AcceptableUsePolicyAcronym,
    url: externalUrls.narrativeAup
  },
  privacy: {
    text: SharedComponentMessages.PrivacyPolicy,
    url: externalUrls.narrativePrivacyPolicy
  }
};

const Legal: React.SFC<LinkLegalProps> = (props) => {
  const { color, target, noFollow, children, type } = props;

  const legalMessageType = legalMessageTypes[type];

  return (
    <StyledLink
      as="a"
      color={color}
      href={legalMessageType.url}
      target={target || '_blank'}
      rel={noFollow ? 'nofollow' : undefined}
    >
      {children || <FormattedMessage {...legalMessageType.text}/>}
    </StyledLink>
  );
};

// Link.TokenSale definition
const tokenSaleMessageTypes = {
  switcheo: {
    text: SharedComponentMessages.Switcheo,
    url: externalUrls.switcheo
  },
  bilaxy: {
    text: SharedComponentMessages.Bilaxy,
    url: externalUrls.bilaxy
  }
};

const TokenSale: React.SFC<LinkTokenSaleProps> = (props) => {
  const { color, target, noFollow, linkText, type } = props;

  const tokenSaleMessageType = tokenSaleMessageTypes[type];

  return (
    <StyledLink
      as="a"
      color={color}
      href={tokenSaleMessageType.url}
      target={target || '_blank'}
      rel={noFollow ? 'nofollow' : undefined}
    >
      {linkText || <FormattedMessage {...tokenSaleMessageType.text}/>}
    </StyledLink>
  );
};

// Link.About definition
const aboutMessageTypes = {
  niche: {
    text: SharedComponentMessages.Niche,
    url: WebRoute.NicheExplainer
  },
  nicheAbout: {
    text: SharedComponentMessages.About,
    url: WebRoute.NicheExplainer
  },
  niches: {
    text: SharedComponentMessages.Niches,
    url: WebRoute.NicheExplainer
  },
  publications: {
    text: SharedComponentMessages.Publications,
    url: WebRoute.PublicationExplainer
  },
  nrve: {
    text: SharedComponentMessages.Nrve,
    url: WebRoute.NRVEExplainer
  },
  rewards: {
    text: SharedComponentMessages.NarrativeRewards,
    url: WebRoute.RewardsExplainer
  },
  certified: {
    text: SharedComponentMessages.Certified,
    url: WebRoute.CertificationExplainer
  },
  certification: {
    text: SharedComponentMessages.Certification,
    url: WebRoute.CertificationExplainer
  },
  nrveWallets: {
    text: SharedComponentMessages.NrveWallets,
    url: WebRoute.NrveWalletsExplainer
  },
};

const About: React.SFC<LinkAboutProps> = (props) => {
  const { target, noFollow, type, children, ...linkProps } = props;

  const aboutMessageType = aboutMessageTypes[type];

  return (
    <StyledLink
      as={RouterLink}
      to={aboutMessageType.url}
      target={target || '_blank'}
      rel={noFollow ? 'nofollow' : undefined}
      {...linkProps}
    >
      {children || <FormattedMessage {...aboutMessageType.text}/>}
    </StyledLink>
  );
};

// exported Link component definition
export class Link extends React.Component<LinkProps, {}> {
  static Anchor = Anchor;
  static Icon = Icon;
  static Legal = Legal;
  static TokenSale = TokenSale;
  static About = About;

  render () {
    const { noFollow, children, ...rest } = this.props;

    const securedProps = secureProps(rest);

    let renderAs: React.ComponentClass | 'a' = RouterLink;
    // jw: if the `to` attribute was stripped off, then the link was secured and we cannot use the RouterLink to render.
    if (!securedProps.to) {
      renderAs = 'a';
    }

    return (
      <StyledLink
        {...securedProps}
        as={renderAs}
        rel={noFollow ? 'nofollow' : undefined}
      >
        {children}
      </StyledLink>
    );
  }
}
