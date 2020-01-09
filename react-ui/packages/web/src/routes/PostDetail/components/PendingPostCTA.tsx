import * as React from 'react';
import { branch, compose, renderNothing } from 'recompose';
import { WithPostByIdProps } from '@narrative/shared';
import {
  withApprovePublicationPostController,
  WithApprovePublicationPostControllerProps
} from '../../../shared/containers/withApprovePublicationPostController';
import {
  withRemovePostFromPublicationController,
  WithRemovePostFromPublicationControllerProps
} from '../../../shared/containers/withRemovePostFromPublicationController';
import { RemovePostFromPublicationModal } from '../../../shared/components/post/RemovePostFromPublicationModal';
import { ApprovePublicationPostModal } from '../../../shared/components/post/ApprovePublicationPostModal';
import { Affix, Alert } from 'antd';
import { PostDetailMessages } from '../../../shared/i18n/PostDetailMessages';
import styled from '../../../shared/styled';
import { FormattedMessage } from 'react-intl';
import { Heading } from '../../../shared/components/Heading';
import { Button } from '../../../shared/components/Button';
import { generatePath } from 'react-router';
import { WebRoute } from '../../../shared/constants/routes';
import { AffixProps } from 'antd/lib/affix';
import { mediaQuery } from '../../../shared/styled/utils/mediaQuery';
import { AlertProps } from 'antd/lib/alert';
import { themeColors } from '../../../shared/styled/theme';
import { LiveOnNetworkTag } from '../../../shared/components/post/LiveOnNetworkTag';

type ParentProps = Pick<WithPostByIdProps, 'post' | 'postDetail'>;

type Props = ParentProps &
  WithRemovePostFromPublicationControllerProps &
  WithApprovePublicationPostControllerProps;

const PendingPostAffix = styled<AffixProps>(Affix)`
  & > .ant-affix {
    ${mediaQuery.md_down`
      bottom: 75px !important;
    `}
  }
`;

const PendingPostAlert = styled<AlertProps>(Alert)`
  &.ant-alert {
    margin-bottom: 50px;
    &.ant-alert-info {
      border: 1px solid ${themeColors.mediumGray};
      background-color: ${themeColors.lightGray};
    }
  }
`;

const CTAContainer = styled.div`
  text-align: center;
  & > *:not(:first-child) {
    margin-left: 7px;
  }
`;

const PendingPostCTAComponent: React.SFC<Props> = (props) => {
  const {
    post,
    postDetail,
    approvePublicationPostModalProps,
    generateApprovePublicationPostButton,
    removePostFromPublicationModalProps,
    generateRemovePostFromPublicationButton,
  } = props;

  if (!approvePublicationPostModalProps || !removePostFromPublicationModalProps) {
    // todo:error-handling: We should never get this far unless the post is not live, and in that case the only people
    //      who can view it should have access to both of these tools.
    return null;
  }

  if (!generateApprovePublicationPostButton || !generateRemovePostFromPublicationButton) {
    // todo:error-handling: We should never get this far unless the post is not live, and in that case the only people
    //      who can view it should have access to both of these tools.
    return null;
  }

  return (
    <React.Fragment>
      <PendingPostAffix offsetTop={10} offsetBottom={10}>
        <PendingPostAlert
          type="info"
          message={
            <React.Fragment>
              <Heading color="white" size={3} style={{textAlign: 'center'}}>
                <FormattedMessage {...PostDetailMessages.PendingPost}/>
                {post.postLive && <LiveOnNetworkTag/>}
              </Heading>
              <CTAContainer>
                {generateApprovePublicationPostButton(
                  'small',
                  PostDetailMessages.ApprovePostInPublicationButtonText
                )}
                {generateRemovePostFromPublicationButton()}
                <Button
                  type="green"
                  href={generatePath(WebRoute.Post, {postOid: postDetail.oid})}
                  size="small"
                >
                  <FormattedMessage {...PostDetailMessages.EditPendingPostLinkText}/>
                </Button>
              </CTAContainer>
            </React.Fragment>
          }
        />
      </PendingPostAffix>
      <RemovePostFromPublicationModal {...removePostFromPublicationModalProps} />
      <ApprovePublicationPostModal {...approvePublicationPostModalProps} />
    </React.Fragment>
  );
};

export const PendingPostCTA = compose(
  // jw: if the post is live then there is no reason to include this component.
  branch<ParentProps>((props) => !props.postDetail.pendingPublicationApproval,
    renderNothing
  ),
  withRemovePostFromPublicationController,
  withApprovePublicationPostController
)(PendingPostCTAComponent) as React.ComponentClass<ParentProps>;
