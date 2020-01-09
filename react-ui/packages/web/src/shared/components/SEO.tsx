import * as React from 'react';
import { Helmet } from 'react-helmet';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { SEOMessages } from '../i18n/SEOMessages';
import { compose } from 'recompose';
import contentRevolutionImage from '../../assets/content-revolution.png';
import { Publication } from '@narrative/shared';
import FormattedMessage = ReactIntl.FormattedMessage;
import { isIntlMessageDescriptor } from '../utils/intlUtils';

// tslint:disable-next-line no-any
function getMetaTags (props: Props): any[] {
  const {
    intl,
    intl: { formatMessage },
    title,
    author,
    authorUrl,
    url,
    imageUrl,
    imageWidth,
    imageHeight,
    description,
    tags,
    publishedTime,
    robots,
    statusCode,
    ogType
  } = props;

  // tslint:disable-next-line no-any
  const metaTags: any[] = [];

  metaTags.push({ property: 'og:site_name', content: intl.formatMessage(SEOMessages.Narrative) });
  metaTags.push({ property: 'twitter:site', content: '@narrative_hq' });

  if (title) {
    metaTags.push({ name: 'title', content: title });
    metaTags.push({ itemprop: 'name', content: title });
    metaTags.push({ property: 'og:title', content: title });
    metaTags.push({ property: 'twitter:title', content: title });
  }

  if (author) {
    metaTags.push({ name: 'author', content: author });
  }

  if (authorUrl) {
    metaTags.push({ name: 'article:author', content: authorUrl });
  }

  if (url) {
    metaTags.push({ property: 'og:url', content: url });
    metaTags.push({ property: 'twitter:url', content: url });
  }

  let imageUrlResolved;
  let imageWidthResolved;
  let imageHeightResolved;
  if (imageUrl) {
    imageUrlResolved = imageUrl;
    imageWidthResolved = imageWidth;
    imageHeightResolved = imageHeight;
  } else {
    imageUrlResolved = contentRevolutionImage;
    imageWidthResolved = 600;
    imageHeightResolved = 589;
  }
  metaTags.push({ itemprop: 'image', content: imageUrlResolved });
  metaTags.push({ property: 'og:image', content: imageUrlResolved });
  metaTags.push({ name: 'twitter:card', content: 'summary_large_image' });
  metaTags.push({ name: 'twitter:image:src', content: imageUrlResolved });
  if (imageWidthResolved && imageHeightResolved) {
    metaTags.push({ name: 'og:image:width', content: imageWidthResolved });
    metaTags.push({ name: 'og:image:height', content: imageHeightResolved });
  }

  if (description) {
    let descriptionResolved: string | undefined;
    if (isIntlMessageDescriptor(description)) {
      descriptionResolved = formatMessage(description);
    } else {
      descriptionResolved = description;
    }
    metaTags.push({ name: 'description', content: descriptionResolved });
    metaTags.push({ itemprop: 'description', content: descriptionResolved });
    metaTags.push({ property: 'og:description', content: descriptionResolved });
    metaTags.push({ property: 'twitter:description', content: descriptionResolved });
  }

  if (robots) {
    metaTags.push({ name: 'robots', content: robots });
  }

  if (statusCode) {
    metaTags.push({ name: 'prerender-status-code', content: `${statusCode}` });
  }

  if (ogType) {
    metaTags.push({ property: 'og:type', content: ogType });
  }

  if (tags && tags.length) {
    tags.forEach((tag: string) => {
      metaTags.push({ property: 'article:tag', content: tag });
    });
  }

  if (publishedTime) {
    metaTags.push({ property: 'article:published_time', content: publishedTime });
  }

  return metaTags;
}

// tslint:enable no-any
export interface MetaTagProps {
  title?: string | FormattedMessage.MessageDescriptor;
  author?: string;
  authorUrl?: string;
  url?: string;
  canonicalUrl?: string;
  ogType?: string;
  imageUrl?: string;
  imageWidth?: number;
  imageHeight?: number;
  omitSuffix?: boolean;
  description?: string | FormattedMessage.MessageDescriptor;
  tags?: string[];
  publishedTime?: string;
  robots?: string;
  statusCode?: number;
  publication?: Publication;
}

type Props =
  MetaTagProps &
  InjectedIntlProps;

const SEOComponent: React.SFC<Props> = (props) => {
  const { intl: { formatMessage }, title, url, canonicalUrl, publication, omitSuffix } = props;

  let canonicalUrlResolved;
  if (canonicalUrl) {
    canonicalUrlResolved = canonicalUrl;
  } else if (url) {
    canonicalUrlResolved = url;
  } else {
    canonicalUrlResolved = window.location.href;
  }

  let titleSuffix = '';
  if (!omitSuffix) {
    if (publication) {
      const publicationName = publication.name;
      titleSuffix += formatMessage(SEOMessages.PublicationSuffix, {publicationName});
    }
    titleSuffix += formatMessage(SEOMessages.Suffix);
  }

  let titleResolved: string | undefined;
  if (title) {
    if (isIntlMessageDescriptor(title)) {
      titleResolved = formatMessage(title);
    } else {
      titleResolved = title;
    }
    titleResolved = titleResolved + titleSuffix;
  }

  return (
    <Helmet
      htmlAttributes={{
        lang: 'en',
        itemscope: undefined,
        itemtype: ''
      }}
      title={titleResolved}
      link={[
        { rel: 'canonical', href: canonicalUrlResolved }
      ]}

      meta={getMetaTags({...props, title: titleResolved})}
    />
  );
};

export const SEO = compose(
  injectIntl
)(SEOComponent) as React.ComponentClass<MetaTagProps>;
