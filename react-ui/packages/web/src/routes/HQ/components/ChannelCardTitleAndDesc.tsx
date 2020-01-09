import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { Heading, HeadingSize } from '../../../shared/components/Heading';
import { Paragraph } from '../../../shared/components/Paragraph';
import { DeletedChannel } from '@narrative/shared';
import { ChannelLink } from '../../../shared/components/channel/ChannelLink';
import styled from '../../../shared/styled';
import { FollowChannelButton } from '../../../shared/components/FollowChannelButton';
import { Channel } from '../../../shared/utils/channelUtils';
import { ChannelStatusTag } from '../../../shared/components/channel/ChannelStatusTag';

const ContentWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 10px;
`;

/*
  jw: Note: If you are going to change this CSS and how it affects the extra-info, be careful about the presentation on
      all pages. This should be tested in at least the Niche Details, Appeal Details and Appeals List UIs. All of them
      have slightly different requirements, and coming up with CSS that properly handles all of them was not trivial.
 */
interface TitleWrapperProps {
  center?: boolean;
  absolutePositionExtraInfo?: boolean;
}
const TitleWrapper = styled<FlexContainerProps & TitleWrapperProps>(FlexContainer)`
  position: relative;
  margin-bottom: 10px;

  .title-extra-info {
    ${props => (props.absolutePositionExtraInfo) && `
      position: absolute;
      top: -5px;
      right: -2.5px;
    `}
  
    h2, span {
      max-width: fit-content;
    }
  }
  
  ${props => props.center && `
    @media screen and (max-width: 539px) {
      align-items: center;
    }
  `};
  
  @media screen and (max-width: 539px) {
    flex-direction: column;

    .title-extra-info {
      position: static;
      margin: 10px 0;
      i {
        align-self: center;
      }
    }
  }
`;

const TitleExtraWrapper = styled<FlexContainerProps>(FlexContainer)`
   
`;

const FollowButtonWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 8px;
`;

interface ParentProps {
  title?: string;
  titleSize?: HeadingSize;
  description?: string;
  center?: boolean;
  channel?: Channel;
  deletedChannel?: DeletedChannel;
  linkPath?: string;
  dontLinkTitle?: boolean;
  forListCard?: boolean;

  /*
   jw: these properties deserve a bit of explanation. First, the niche status tag and 'extra' are grouped together into
       what has become known as the 'extra info'. This is primary used for the status tag, and we want it to be
       vertically center aligned with the title, right beside it in most places. In others, like where the title is
       centered, or the niche details, we want it to float off to the right. Hence the absolutePositionExtraInfo. This
       gives pages the ability to force the extra-info off to the right regardless of centering or other factors.
   */
  includeStatusTag?: boolean;
  absolutePositionExtraInfo?: boolean;
}

export const ChannelCardTitleAndDesc: React.SFC<ParentProps> = (props) => {
  let { title, description, dontLinkTitle } = props;
  const { titleSize, channel, deletedChannel, linkPath, center, includeStatusTag, forListCard } = props;
  const { absolutePositionExtraInfo } = props;

  // jw: if we were given a channel, always use the channel to define the title and description
  if (channel) {
    title = channel.name;
    description = channel.description || undefined;
  } else if (deletedChannel) {
    title = deletedChannel.name;
    dontLinkTitle = true;
  }

  // jw: if this is for a list card we want to use 16px titles, and 12px descriptions
  let titleStyle;
  let descriptionStyle;
  if (forListCard) {
    titleStyle = { fontSize: '16px'};
    descriptionStyle = { fontSize: '12px'};
  }

  const textAlign = center ? 'center' : 'left';
  const headingText = channel && !dontLinkTitle
    ? <ChannelLink size="inherit" channel={channel} linkPath={linkPath}>{title}</ChannelLink>
    : title;

  const channelResolved = channel || deletedChannel;

  return (
    <ContentWrapper column={true}>
      <TitleWrapper
        alignItems="center"
        justifyContent={center ? 'center' : undefined}
        center={center}
        // jw: we need to absolutely position the extra info if we are centering our title, or we were told to.
        //     This is vital for centering, otherwise the positioning of the title is thrown off.
        absolutePositionExtraInfo={center || absolutePositionExtraInfo}
      >
        <Heading
          size={titleSize || 4}
          weight={600}
          isLink={channel && !dontLinkTitle}
          textAlign={textAlign}
          noMargin={true}
          style={titleStyle}
        >
          {headingText}
        </Heading>

        {includeStatusTag && channelResolved &&
          <TitleExtraWrapper className="title-extra-info" column={true}>
            <ChannelStatusTag channel={channelResolved} marginLeft="small" />
          </TitleExtraWrapper>
        }
      </TitleWrapper>

      {channel &&
      <FollowButtonWrapper centerAll={center}>
        <FollowChannelButton channel={channel}/>
      </FollowButtonWrapper>}

      <Paragraph style={descriptionStyle} textAlign={textAlign}>
        {description}
      </Paragraph>
    </ContentWrapper>
  );
};
