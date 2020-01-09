import * as React from 'react';
import { PostProps } from '../contentStream/ContentStreamItem';
import { TextSize, TextSizes } from '../Text';
import styled, { css } from '../../styled';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { MemberAvatar } from '../user/MemberAvatar';
import { MemberLink } from '../user/MemberLink';
import { LocalizedTime } from '../LocalizedTime';
import { FormattedMessage } from 'react-intl';
import { ContentStreamMessages } from '../../i18n/ContentStreamMessages';
import { PublicationLink } from '../publication/PublicationLink';

interface BylineStylingProps {
  textSize?: TextSize;
  forPublicationDisplay?: boolean;
  forPublicationReview?: boolean;
}

type StyledBylineProps = FlexContainerProps & BylineStylingProps;
function getTextSize(props: BylineStylingProps) {
  if (props.textSize) {
    return css`
      &, & a {
        ${TextSizes[props.textSize]}
      }
    `;
  }
  return null;
}

const StyledByline = styled<StyledBylineProps>(({textSize, ...rest}) => <FlexContainer {...rest} />)`
  margin-top: 10px;
  
  ${p => getTextSize(p)}
`;

const BylineColumn = styled<FlexContainerProps>(FlexContainer)`
  &:not(:first-child) {
    margin-left: 5px;
  }
`;

const DateContainer = styled.div`
  margin-top: 5px;
`;

type Props =
  PostProps &
  BylineStylingProps;

export const PostByline: React.SFC<Props> = (props) => {
  const { post, textSize, forPublicationDisplay, forPublicationReview } = props;

  return (
    <StyledByline alignItems="center" textSize={textSize}>
      <BylineColumn>
        <MemberAvatar user={post.author} size={50} />
      </BylineColumn>
      <BylineColumn direction="column">
        <div>
          <MemberLink user={post.author} color="dark" />
          {!forPublicationDisplay && post.publishedToPublication &&
            <FormattedMessage
              {...ContentStreamMessages.InPublication}
              values={{
                publicationLink: <PublicationLink publication={post.publishedToPublication} itemProp="articleSection" />
              }}
            />
          }
        </div>
        <DateContainer>
          {forPublicationReview && post.moderationDatetime ?
            <LocalizedTime time={post.moderationDatetime} /> :
            <LocalizedTime time={post.liveDatetime} />
          }
        </DateContainer>
      </BylineColumn>
    </StyledByline>
  );
};
