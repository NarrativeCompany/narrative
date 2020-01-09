import * as React from 'react';
import { SearchResult } from '@narrative/shared';
import { SearchMessages } from '../../shared/i18n/SearchMessages';
import { FormattedMessage } from 'react-intl';
import { FlexContainer } from '../../shared/styled/shared/containers';
import { SearchItemRow } from './SearchItemRow';
import { NicheLink } from '../../shared/components/niche/NicheLink';
import { MemberLink } from '../../shared/components/user/MemberLink';
import { LocalizedTime } from '../../shared/components/LocalizedTime';
import { LinkStyleProps } from '../../shared/components/Link';
import { MemberAvatar } from '../../shared/components/user/MemberAvatar';
import { PostLink } from '../../shared/components/post/PostLink';
import { PostAvatar } from '../../shared/components/post/PostAvatar';
import { PublicationLink } from '../../shared/components/publication/PublicationLink';
import { PublicationAvatar } from '../../shared/components/publication/PublicationAvatar';
import { ChannelStatusTag } from '../../shared/components/channel/ChannelStatusTag';

// jw: to ensure consistent styling for the results, since the different types of links have different default styles
//     I am going to create a object representation of how we want them styled for search results. This gives us a
//     spreadable object that we can use on each link to ensure consistency.
const linkStyling: LinkStyleProps = {
  size: 20,
  color: 'default',
  weight: 600
};

const avatarSize = 80;

interface Props {
  searchResult: SearchResult;
  hideTypeLabel?: boolean;
}

export const SearchItem: React.SFC<Props> = (props) => {
  const { searchResult, hideTypeLabel } = props;

  // jw: let's handle if this search result is a niche.
  if (searchResult.niche) {
    const { niche } = searchResult;
    return (
      <SearchItemRow
        type={hideTypeLabel ? undefined : SearchMessages.Title_Niche}
        name={(
          <FlexContainer alignItems="center">
            <NicheLink {...linkStyling} niche={niche}/>
            <ChannelStatusTag channel={niche} size="small" style={{ marginLeft: 10, maxHeight: 18 }}/>
          </FlexContainer>
        )}
        description={niche.description || ''}
      />
    );
  }

  // jw: let's handle if this search result is a publication.
  if (searchResult.publication) {
    const { publication } = searchResult;
    return (
      <SearchItemRow
        type={hideTypeLabel ? undefined : SearchMessages.Title_Publication}
        name={<PublicationLink {...linkStyling} publication={publication}/>}
        description={publication.description || ''}
        avatar={<PublicationAvatar publication={publication} size={avatarSize} />}
      />
    );
  }

  // jw: next, let's try and see if the result is a UserDetail
  if (searchResult.userDetail) {
    const { userDetail } = searchResult;

    const joined = <LocalizedTime time={userDetail.joined} dateOnly={true} />;
    let description = <FormattedMessage {...SearchMessages.Joined} values={{joined}}/>;

    if (userDetail.lastVisit) {
      const lastVisit = <LocalizedTime time={userDetail.lastVisit}/>;
      const lastVisitText = <FormattedMessage {...SearchMessages.LastVisit} values={{ lastVisit }}/>;

      description = (
        <React.Fragment>
          {description} &nbsp;|&nbsp; {lastVisitText}
        </React.Fragment>
      );
    }
    return (
      <SearchItemRow
        type={hideTypeLabel ? undefined : SearchMessages.Title_Member}
        name={<MemberLink {...linkStyling} user={userDetail.user} appendUsername={true} dontApplySizeToBadge={true} />}
        avatar={<MemberAvatar user={userDetail.user} size={avatarSize} />}
        description={description}
      />
    );
  }

  const { post } = searchResult;
  // jw: finally, we expect it to be a post.
  if (!post) {
    // todo:error-handling: We need to report an unexpected search result type here.
    return null;
  }

  return (
    <SearchItemRow
      type={hideTypeLabel ? undefined : SearchMessages.Title_Post}
      name={<PostLink {...linkStyling} post={post} />}
      avatar={<PostAvatar post={post} size={avatarSize} />}
      description={post.subTitle}
    />
  );
};
