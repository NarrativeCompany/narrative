import * as React from 'react';
import { Link } from '../../shared/components/Link';
import { FormattedMessage } from 'react-intl';
import { FooterMessages } from '../../shared/i18n/FooterMessages';
import * as moment from 'moment';
import styled from '../../shared/styled';
import { externalUrls } from '../../shared/constants/externalUrls';
import { FlexContainer, FlexContainerProps } from '../../shared/styled/shared/containers';

const links = [
  { url: externalUrls.narrativeAboutWebsite, text: <FormattedMessage {...FooterMessages.AboutLinkText}/> },
  { url: externalUrls.narrativeBlog, text: <FormattedMessage {...FooterMessages.BlogLinkText}/> },
  {
    url: externalUrls.narrativeSupportCommunity,
    text: <FormattedMessage {...FooterMessages.SupportCommunityLinkText}/>
  }
];

const htmlLinks: React.ReactElement[] = [];

links.forEach((link) => {
  htmlLinks.push(
    <Link.Anchor href={link.url} target="_blank">
      {link.text}
    </Link.Anchor>
  );
});

htmlLinks.push(<Link.Legal type="tos"/>);
htmlLinks.push(<Link.Legal type="aup"/>);
htmlLinks.push(<Link.Legal type="privacy"/>);

const FooterContentWrapper = styled<FlexContainerProps>(FlexContainer)`
  background: ${props => props.theme.bgBlackSecondary};
  padding: 10px 24px;
  flex-wrap: wrap;
  
  > a {
    margin-right: 20px;
  }
  
  > span {
    color: #fff;
  }
`;

export const FooterContent: React.SFC<{}> = () => {
  const currentYear = moment().year();

  return (
    <FooterContentWrapper centerAll={true}>
      {htmlLinks.map((link, i) => {
        return {...link, key: i};
      })}
      <FormattedMessage
        {...FooterMessages.CopyrightMessage}
        values={{currentYear}}
      />
    </FooterContentWrapper>
  );
};
