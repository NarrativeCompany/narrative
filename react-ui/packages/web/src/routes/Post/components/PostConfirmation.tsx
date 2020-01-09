import * as React from 'react';
import { compose } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { StyledViewWrapper } from '../Post';
import { PageHeader } from '../../../shared/components/PageHeader';
import { PostLocation } from './PostLocation';
import { Heading } from '../../../shared/components/Heading';
import { Button } from '../../../shared/components/Button';
import { PostMessages } from '../../../shared/i18n/PostMessages';
import { themeTypography } from '../../../shared/styled/theme';
import { withRenderCurrentSlide } from '../../../shared/containers/withCarouselController';
import { User, PostDetail, Niche, PublicationDetail } from '@narrative/shared';
import styled from '../../../shared/styled';
import { getPostUrl } from '../../../shared/utils/postUtils';
import { generatePath } from 'react-router';
import { WebRoute } from '../../../shared/constants/routes';
import { ApprovePublicationPostButton } from './ApprovePublicationPostButton';
import { PublicationDetailsContextProvider } from '../../Publication/components/PublicationDetailsContextProvider';

export const postConfirmationContentBodyWidth = 325;

const PostConfirmationWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-top: 80px;
`;

const ContentBody = styled<FlexContainerProps>(FlexContainer)`
  width: ${postConfirmationContentBodyWidth}px;
  & > * {
    width: 100%;
  }
`;

const ApprovePostButtonWrapper = styled.div`
  margin-top: 20px;
  & > button {
    width: ${postConfirmationContentBodyWidth}px;
  }
`;

interface ParentProps {
  currentSlide: number;
  currentUser: User;
  postDetail: PostDetail;
  selectedNiches: Niche[];
  selectedPublicationDetail?: PublicationDetail;
  publishToPrimaryChannel?: string;
  postPrettyUrlString: string;
  postOid: string;
  postLive: boolean;
  edit?: boolean;
  pendingPublicationApproval: boolean;
  authorPersonalJournalOid: string;
}

const PostConfirmationComponent: React.SFC<ParentProps> = (props) => {
  const {
    currentUser,
    postDetail,
    postPrettyUrlString,
    selectedNiches,
    selectedPublicationDetail,
    authorPersonalJournalOid,
    postLive,
    edit,
    pendingPublicationApproval,
    publishToPrimaryChannel,
    postOid
  } = props;

  const hasNiches = selectedNiches.length > 0;
  const isPersonalJournal = publishToPrimaryChannel === authorPersonalJournalOid;

  const description = (hasNiches || publishToPrimaryChannel)
    ? (
      <FormattedMessage {...(postLive
        ? PostMessages.PostConfirmationPageHeaderDescription
        : PostMessages.PostPendingConfirmationPageHeaderDescription
      )}/>
    )
    : '';

  let to: string;
  let buttonMessage: FormattedMessage.MessageDescriptor;

  if (!postLive && !edit) {
    to = generatePath(WebRoute.MemberManagePendingPosts);
    buttonMessage = PostMessages.ViewPendingPostBtnText;
  } else {
    to = getPostUrl(postPrettyUrlString, postOid);
    buttonMessage = PostMessages.ViewPostBtnText;
  }

  return (
    <StyledViewWrapper>
      <PostConfirmationWrapper column={true} alignItems="center">
        <PageHeader
          title={<FormattedMessage {...(postLive
            ? PostMessages.PostConfirmationPageHeaderTitle
            : PostMessages.PostPendingConfirmationPageHeaderTitle
          )}/>}
          description={description}
          center="all"
          size={2}
          style={{ marginBottom: 15 }}
        />

        <ContentBody
          column={true}
          alignItems="flex-start"
          style={{ margin: !hasNiches && !isPersonalJournal && !selectedPublicationDetail ? 0 : '20px 0' }}
        >
          {isPersonalJournal && <PostLocation personalJournal={true} status="connected"/>}

          {selectedPublicationDetail &&
          <React.Fragment>
            <Heading size={6} uppercase={true} color={themeTypography.textColor} style={{ marginBottom: 10 }}>
              <FormattedMessage {...PostMessages.PostLocationPublicationTitle}/>
            </Heading>
            <PostLocation
              key={selectedPublicationDetail.oid}
              publication={selectedPublicationDetail.publication}
              status={pendingPublicationApproval ? 'pending' : 'connected'}/>
          </React.Fragment>
          }

          {hasNiches &&
          <Heading size={6} uppercase={true} color={themeTypography.textColor} style={{ marginBottom: 10 }}>
            <FormattedMessage
              {...PostMessages.PostLocationNichesTitle}
              values={{ nicheCount: selectedNiches.length }}
            />
          </Heading>}

          {selectedNiches.map(niche => (
            <PostLocation key={niche.oid} niche={niche} status={!postLive ? 'pending' : 'connected'}/>
          ))}
        </ContentBody>

        <Button
          type="primary"
          size="large"
          style={{ minWidth: postConfirmationContentBodyWidth }}
          href={to}
        >
          <FormattedMessage {...buttonMessage}/>
        </Button>
        {selectedPublicationDetail &&
          <PublicationDetailsContextProvider publicationDetail={selectedPublicationDetail} currentUser={currentUser}>
            <ApprovePostButtonWrapper>
              <ApprovePublicationPostButton postDetail={postDetail}/>
            </ApprovePostButtonWrapper>
          </PublicationDetailsContextProvider>
        }
      </PostConfirmationWrapper>
    </StyledViewWrapper>
  );
};

export const PostConfirmation = compose(
  withRenderCurrentSlide(2)
)(PostConfirmationComponent) as React.ComponentClass<ParentProps>;
