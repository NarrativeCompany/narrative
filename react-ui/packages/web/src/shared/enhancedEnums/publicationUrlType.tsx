import * as React from 'react';
import { PublicationUrls, PublicationDetail } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { FormattedMessage } from 'react-intl';
import { Omit } from 'recompose';
import { PublicationDetailsMessages } from '../i18n/PublicationDetailsMessages';
import { Icon, SVG_COMPONENTS, SvgComponent } from '../components/Icon';
import { CSSProperties } from 'react';
import { Link } from '../components/Link';

// jw: this is only here because at the time of writing it the PublicationSchema.graphql instance is not being
//     referenced, and as a result is not being exported. Once that is referenced at least once it should be exported
//     and we can remove this in favor of importing from shared.
export enum PublicationUrlType {
  WEBSITE = 'WEBSITE',
  TWITTER = 'TWITTER',
  FACEBOOK = 'FACEBOOK',
  INSTAGRAM = 'INSTAGRAM',
  YOUTUBE = 'YOUTUBE',
  SNAPCHAT = 'SNAPCHAT',
  PINTEREST = 'PINTEREST',
  LINKED_IN = 'LINKED_IN'
}

export function getPublicationSocialLinks(details: PublicationDetail): React.ReactNode[] | undefined {
  const { urls } = details;

  const links: React.ReactNode[] = [];

  EnhancedPublicationUrlType.enhancers.forEach((helper) => {
    const url = helper.getUrl(urls);

    if (url) {
      links.push(
        <Link.Anchor href={url} target="_blank">
          {helper.getIcon()}
        </Link.Anchor>
      );
    }
  });

  if (!links.length) {
    return undefined;
  }

  return links;
}

type TrimmedPublicationUrls = Omit<PublicationUrls, '__typename'>;
type UrlGetterFunction = (urls: TrimmedPublicationUrls) => string | null;

// jw: let's define the PublicationUrlTypeHelper that will provide all the extra helper logic for PublicationUrlTypes
export class PublicationUrlTypeHelper {
  urlType: PublicationUrlType;
  title: FormattedMessage.MessageDescriptor;
  urlGetter: UrlGetterFunction;
  urlFieldName: keyof TrimmedPublicationUrls;
  // jw: this kills me a little bit, but it looks like AntD does not have a concrete type for their icons.
  iconType: string | SvgComponent;
  fillIcon?: boolean;

  constructor(
    urlType: PublicationUrlType,
    title: FormattedMessage.MessageDescriptor,
    urlGetter: UrlGetterFunction,
    urlFieldName: keyof TrimmedPublicationUrls,
    iconType: string,
    fillIcon?: boolean
  ) {
    this.urlType = urlType;
    this.title = title;
    this.iconType = iconType;
    this.urlFieldName = urlFieldName;
    this.urlGetter = urlGetter;
    this.fillIcon = fillIcon;
  }

  getUrl(urls: TrimmedPublicationUrls): string | undefined {
    return this.urlGetter(urls) || undefined;
  }

  getIcon(style?: CSSProperties): React.ReactNode {
    const isSvgIcon = this.iconType in SVG_COMPONENTS;

    return (
      <Icon
        type={isSvgIcon ? undefined : this.iconType}
        svgIcon={isSvgIcon ? this.iconType as SvgComponent : undefined}
        style={style}
      />
    );
  }
}

// jw: next: lets create the lookup of PublicationUrlType to helper object

const helpers: {[key: number]: PublicationUrlTypeHelper} = [];
// jw: make sure to register these in the order you want them to display.
helpers[PublicationUrlType.WEBSITE] = new PublicationUrlTypeHelper(
  PublicationUrlType.WEBSITE,
  PublicationDetailsMessages.WebsiteUrlTitle,
  (urls) => urls.websiteUrl,
  'websiteUrl',
  'home',
  true
);
helpers[PublicationUrlType.TWITTER] = new PublicationUrlTypeHelper(
  PublicationUrlType.TWITTER,
  PublicationDetailsMessages.TwitterUrlTitle,
  (urls) => urls.twitterUrl,
  'twitterUrl',
  'twitter'
);
helpers[PublicationUrlType.FACEBOOK] = new PublicationUrlTypeHelper(
  PublicationUrlType.FACEBOOK,
  PublicationDetailsMessages.FacebookUrlTitle,
  (urls) => urls.facebookUrl,
  'facebookUrl',
  'facebook',
  true
);
helpers[PublicationUrlType.INSTAGRAM] = new PublicationUrlTypeHelper(
  PublicationUrlType.INSTAGRAM,
  PublicationDetailsMessages.InstagramUrlTitle,
  (urls) => urls.instagramUrl,
  'instagramUrl',
  'instagram'
);
helpers[PublicationUrlType.YOUTUBE] = new PublicationUrlTypeHelper(
  PublicationUrlType.YOUTUBE,
  PublicationDetailsMessages.YoutubeUrlTitle,
  (urls) => urls.youtubeUrl,
  'youtubeUrl',
  'youtube',
  true
);
helpers[PublicationUrlType.SNAPCHAT] = new PublicationUrlTypeHelper(
  PublicationUrlType.SNAPCHAT,
  PublicationDetailsMessages.SnapchatUrlTitle,
  (urls) => urls.snapchatUrl,
  'snapchatUrl',
  'snapchat',
  true
);
helpers[PublicationUrlType.PINTEREST] = new PublicationUrlTypeHelper(
  PublicationUrlType.PINTEREST,
  PublicationDetailsMessages.PinterestUrlTitle,
  (urls) => urls.pinterestUrl,
  'pinterestUrl',
  'pinterest',
  true
);
helpers[PublicationUrlType.LINKED_IN] = new PublicationUrlTypeHelper(
  PublicationUrlType.LINKED_IN,
  PublicationDetailsMessages.LinkedInUrlTitle,
  (urls) => urls.linkedInUrl,
  'linkedInUrl',
  'linkedin',
  true
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedPublicationUrlType = new EnumEnhancer<PublicationUrlType, PublicationUrlTypeHelper>(
  helpers
);
