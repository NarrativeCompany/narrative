import * as React from 'react';
import { ReferendumVote } from '@narrative/shared';
import { EnhancedReferendumVoteReason } from '../../enhancedEnums/referendumVoteReason';
import { Icon, Tooltip } from 'antd';
import { Heading } from '../Heading';
import { ReferendumMessages } from '../../i18n/ReferendumMessages';
import { FormattedMessage } from 'react-intl';
import { Paragraph } from '../Paragraph';
import { Link } from '../Link';
import { ReferendumVoteReason } from '@narrative/shared';

interface Props {
  vote: ReferendumVote;
}

export const ReferendumVoteReasonIcon: React.SFC<Props> = (props) => {
  const { vote: { reason, comment } } = props;

  // jw: if we don't have a reason, short out!
  if (!reason) {
    return null;
  }

  // jw: let's get the enhanced version of the reason to make our lives easier.
  const enhancedReason = EnhancedReferendumVoteReason.get(reason);

  let message;
  if (enhancedReason.reason === ReferendumVoteReason.VIOLATES_TOS) {
    const termsOfService = <Link.Legal type="tos"/>;
    const acceptableUsePolicy = <Link.Legal type="aup"/>;
    message = <FormattedMessage {...enhancedReason.radioMessage} values={{termsOfService, acceptableUsePolicy}}/>;
  } else {
    message = <FormattedMessage {...enhancedReason.radioMessage}/>;
  }

  const title = (
    <React.Fragment>
      <Heading size={5} color="white"><FormattedMessage {...ReferendumMessages.ReasonForRejection} /></Heading>

      <Paragraph size="small" color="white" marginBottom="small">
        {message}
      </Paragraph>

      {comment && <Paragraph size="small" color="white" fontStyle="italic">"{comment}"</Paragraph>}
    </React.Fragment>
  );

  return (
    <Tooltip title={title} placement="rightTop">
      <Icon type="info-circle" style={{marginLeft: '5px'}} />
    </Tooltip>
  );
};
