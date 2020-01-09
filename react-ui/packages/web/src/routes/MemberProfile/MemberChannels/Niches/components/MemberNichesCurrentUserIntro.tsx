import * as React from 'react';
import { MemberChannelsMessages } from '../../../../../shared/i18n/MemberChannelsMessages';
import { FormattedMessage } from 'react-intl';
import { Link } from '../../../../../shared/components/Link';
import { WebRoute } from '../../../../../shared/constants/routes';
import { MAX_NICHES } from '../../../../../shared/constants/constants';
import { Paragraph } from '../../../../../shared/components/Paragraph';

interface ParentProps {
  nicheCount: number;
}

export const MemberNichesCurrentUserIntro: React.SFC<ParentProps> = (props) => {
  const { nicheCount } = props;
  // todo: we need a Text component that centralizes our formatting style for text. Using strong until we have that.
  const nicheCountHtml = <strong>{nicheCount}/{MAX_NICHES}</strong>;

  let suffixWording: React.ReactNode | undefined;
  if (nicheCount < MAX_NICHES) {
    const message = nicheCount === 0
      ? MemberChannelsMessages.AcquireFirstNiche
      : MemberChannelsMessages.AcquireMoreNiches;

    const messageValues = {
      suggestLink: <Link to={WebRoute.SuggestNiche}><FormattedMessage {...MemberChannelsMessages.Suggest} /></Link>,
      reviewLink: <Link to={WebRoute.Approvals}><FormattedMessage {...MemberChannelsMessages.Review} /></Link>,
      bidLink: <Link to={WebRoute.Auctions}><FormattedMessage {...MemberChannelsMessages.Bid} /></Link>
    };

    suffixWording = (
      <React.Fragment>
        {' '}
        <FormattedMessage {...message} values={messageValues} />
      </React.Fragment>
    );
  }

  return (
    <Paragraph color="dark">
      <FormattedMessage {...MemberChannelsMessages.MemberNichesDescription} values={{nicheCountHtml}} />
      {suffixWording}
    </Paragraph>
  );
};
