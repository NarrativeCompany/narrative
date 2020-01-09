import * as React from 'react';
import {
  FacebookShareButton, FacebookIcon,
  TwitterShareButton, TwitterIcon,
  LinkedinShareButton, LinkedinIcon,
  RedditShareButton, RedditIcon,
  IconComponentProps
} from 'react-share';
import { CSSProperties } from 'react';
import { FlexContainer } from '../styled/shared/containers';
import { getBaseUrl } from '@narrative/shared';

export interface ShareIconsProps {
  path: string;
  title: string;
  description?: string;
  buttonSize?: number;
}

const iconProps: IconComponentProps = {
  round: true,
  size: 30
};

// jw: there are times we will want to include the share buttons along side other elements, which you cannot do in a
//     component without introducing a container around all of the share buttons. With that in mind, I am writing this
//     utility function to generate the buttons as a collection, and then we can map it where we want.
export function getShareButtons(props: ShareIconsProps): React.ReactNode[] {
  const { path, title, description, buttonSize } = props;

  const baseUrl = getBaseUrl();
  if (!baseUrl) {
    // todo:error-handling: we should always have a baseUrl
    return [];
  }

  const url = baseUrl + path;

  const size = buttonSize || 25;
  const style: CSSProperties = {
    width: `${size}px`,
    height: `${size}px`,
    cursor: 'pointer'
  };

  const sharedProps = { url, style };

  // jw: The definitions for all of these can be found here: https://github.com/nygardk/react-share
  return [(
    <FacebookShareButton {...sharedProps} quote={title}>
      <FacebookIcon {...iconProps}/>
    </FacebookShareButton>
  ), (
    <TwitterShareButton {...sharedProps} title={title}>
      <TwitterIcon {...iconProps}/>
    </TwitterShareButton>
  ), (
    <LinkedinShareButton {...sharedProps} title={title} description={description}>
      <LinkedinIcon {...iconProps}/>
    </LinkedinShareButton>
  ), (
    <RedditShareButton {...sharedProps} title={title}>
      <RedditIcon {...iconProps}/>
    </RedditShareButton>
  )];
}

export const ShareIcons: React.SFC<ShareIconsProps> = (props) => {
  const shareButtons = getShareButtons(props);

  return (
    <FlexContainer alignItems="center" justifyContent="space-between" style={{maxWidth: '200px'}}>
      {shareButtons.map((button, i) => (
        <React.Fragment key={i}>{button}</React.Fragment>
      ))}
    </FlexContainer>
  );
};
