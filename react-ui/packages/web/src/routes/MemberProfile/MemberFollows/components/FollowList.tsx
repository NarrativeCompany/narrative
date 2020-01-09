import * as React from 'react';
import { compose, withProps } from 'recompose';
import { FollowableObject, FollowedUser, WithFollowsProps, chunkArray } from '@narrative/shared';
import { isOfType } from '../../../../apolloClientInit';
import { Card } from '../../../../shared/components/Card';
import { LoadMoreButton } from '../../../../shared/components/LoadMoreButton';
import {
  withLoadMoreButtonController,
  WithLoadMoreButtonControllerParentProps, WithLoadMoreButtonControllerProps
} from '../../../../shared/containers/withLoadMoreButtonController';
import { FlexContainer, FlexContainerProps } from '../../../../shared/styled/shared/containers';
import styled from '../../../../shared/styled';
import { mediaQuery } from '../../../../shared/styled/utils/mediaQuery';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../../shared/context/MemberProfileContext';
import { SEO } from '../../../../shared/components/SEO';
import { ContainedLoading } from '../../../../shared/components/Loading';
import { Channel } from '../../../../shared/utils/channelUtils';

const Container = styled.div`
  margin-top: 20px;
`;

const ItemRow = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 20px;
  
  & > div {
  // jw: we are spacing each card by 20, since there are two spaces let's reduce the 33% by 10 to balance it out.
    width: calc(33% - 10px);
    &:not(:last-child) {
      margin-right: 20px;
    }
  }

  ${mediaQuery.md_down`
    flex-direction: column;
    & > div {
      width: 100%;
      &:not(:last-child) {
        margin-right: 0;
        margin-bottom: 20px;
      }
    }
  `};
`;

function renderItem(item: FollowableObject) {
  if (isOfType(item, 'FollowedUser')) {
    const followedUser = item as FollowedUser;
    const { user, currentUserFollowedItem } = followedUser;

    return <Card.User key={user.oid} user={user} followedUser={currentUserFollowedItem}/>;
  }

  if (isOfType(item, 'Niche') || isOfType(item, 'Publication')) {
    const channel = item as Channel;
    return <Card.Channel key={channel.oid} channel={channel}/>;
  }

  // todo:error-handling: We should never get here, all item types should be rendered above.
  return null;
}

export interface FollowListWordingProps {
  seoTitle: FormattedMessage.MessageDescriptor;
  seoDescription: FormattedMessage.MessageDescriptor;
  noResultsMessage?: FormattedMessage.MessageDescriptor;
  noResultsMessageForCurrentUser?: FormattedMessage.MessageDescriptor;
}

type ParentProps = WithFollowsProps<FollowableObject> &
  FollowListWordingProps;

type Props =
  ParentProps &
  WithLoadMoreButtonControllerProps &
  WithMemberProfileProps &
  InjectedIntlProps;

const FollowListComponent: React.SFC<Props> = (props) => {
  const {
    items,
    loadMoreItemsLoading,
    loadMoreButtonProps,
    isForCurrentUser,
    seoTitle,
    seoDescription,
    noResultsMessage,
    noResultsMessageForCurrentUser,
    detailsForProfile: { user: { displayName } },
    intl: { formatMessage }
  } = props;

  // jw: the only time we want to show the loading spinner is if this is the first time we are loading. Otherwise,
  //     we can just display the results we already have until new ones come in.
  if (loadMoreItemsLoading && !items.length) {
    return <ContainedLoading />;
  }

  let body: React.ReactNode | undefined;
  if (items.length) {
    // jw: because we want to use a flex-container to ensure that all cards grow to fit the tallest neighbor, we need to
    //     chunk the list into groups of 3 so we can collapse it when the viewport gets too small.
    const chunkedItems: FollowableObject[][] = chunkArray(items, 3);

    body = (
      <React.Fragment>
        {chunkedItems.map((itemChunk, i) =>
          <ItemRow key={`followItemsRow${i}`}>
            {itemChunk.map((item) => renderItem(item))}
          </ItemRow>
        )}
        {loadMoreButtonProps && <LoadMoreButton {...loadMoreButtonProps}/>}
      </React.Fragment>
    );

  } else if (isForCurrentUser) {
    if (noResultsMessageForCurrentUser) {
      body = <FormattedMessage {...noResultsMessageForCurrentUser}/>;
    }

  } else if (noResultsMessage) {
    body = <FormattedMessage {...noResultsMessage} values={{displayName}}/>;
  }

  return (
    <Container>
      <SEO
        title={formatMessage(seoTitle, {displayName})}
        description={formatMessage(seoDescription, {displayName})}
      />

      {body}
    </Container>
  );
};

export const FollowList = compose(
  withProps<WithLoadMoreButtonControllerParentProps, WithFollowsProps<FollowableObject>>((props) => {
    const loadMore = props.loadMoreItems;

    return { loadMore };
  }),
  withLoadMoreButtonController,
  MemberProfileConnect,
  injectIntl
)(FollowListComponent) as React.ComponentClass<ParentProps>;
