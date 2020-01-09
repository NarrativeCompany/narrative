import * as React from 'react';
import { Col, Row } from 'antd';
import styled from '../../../shared/styled';
import { compose, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { TribunalIssue, DeletedChannel } from '@narrative/shared';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { TribunalAppealCardMessages } from '../../../shared/i18n/TribunalAppealCardMessages';
import { WebRoute } from '../../../shared/constants/routes';
import { Link } from '../../../shared/components/Link';
import { generatePath } from 'react-router';
import { ChannelCardTitleAndDesc } from '../components/ChannelCardTitleAndDesc';
import { commentsAnchor } from '../../../shared/components/comment/CommentsSection';
import { Channel } from '../../../shared/utils/channelUtils';

const CardHeader = styled<FlexContainerProps>(FlexContainer)`
  justify-content: space-between;
  padding: 23px 32px 12px 32px;
`;

interface ParentProps {
  issue: TribunalIssue;
}

interface WithProps {
  tribunalIssueOid: string;
  commentCount: number;
  channel?: Channel;
  deletedChannel?: DeletedChannel;
}

type Props =
  WithProps &
  ParentProps;

const TribunalAppealCardHeaderComponent: React.SFC<Props> = (props) => {
  const { tribunalIssueOid, channel, deletedChannel, commentCount } = props;

  let toDetails = generatePath(WebRoute.AppealDetails, {tribunalIssueOid});
  if (commentCount > 0) {
    toDetails = `${toDetails}#${commentsAnchor}`;
  }

  return (
    <CardHeader>
      <Row type="flex" justify="space-between" style={{width: '100%'}}>
        <Col>
          <ChannelCardTitleAndDesc
            channel={channel}
            deletedChannel={deletedChannel}
            includeStatusTag={true}
          />
        </Col>

        <Col>
          <Link textDecoration="underline" to={toDetails}>
            <FormattedMessage {...TribunalAppealCardMessages.AppealDetailsCommentCount} values={{count: commentCount}}/>
          </Link>
        </Col>
      </Row>
    </CardHeader>
  );
};

export const TribunalAppealCardHeader = compose(
  withProps((props: Props) => {
    const { issue } = props;

    const tribunalIssueOid =
      issue &&
      issue.oid;

    const referendum =
      issue &&
      issue.referendum;

    const niche =
      referendum &&
      referendum.niche;

    const publication =
      referendum &&
      referendum.publication;

    const deletedChannel =
      referendum &&
      referendum.deletedChannel;

    const channel = niche || publication;

    const commentCount =
      referendum &&
      referendum.commentCount;

    return { tribunalIssueOid, channel, deletedChannel, commentCount };
  })
)(TribunalAppealCardHeaderComponent) as React.ComponentClass<ParentProps>;
