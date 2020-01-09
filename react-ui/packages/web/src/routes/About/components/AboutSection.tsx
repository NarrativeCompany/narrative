import * as React from 'react';
import { FormattedMessage, MessageValue } from 'react-intl';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { Heading } from '../../../shared/components/Heading';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import { themeColors } from '../../../shared/styled/theme';
import styled from '../../../shared/styled';
import { AboutSectionParagraph } from './AboutSectionParagraph';
import { isIntlMessageDescriptor } from '../../../shared/utils/intlUtils';

const AboutSectionWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 60px;
  
  h3 {
    margin-bottom: 20px;
  }
  
  p:not(:last-child) {
    margin-bottom: 20px;
  }
`;

const HighlightTitleTextTypes = {
  nrve: SharedComponentMessages.Nrve,
  niche: SharedComponentMessages.Niche,
  niches: SharedComponentMessages.Niches,
  certified: SharedComponentMessages.Certified
};

type AboutSectionTitleType = keyof typeof HighlightTitleTextTypes;

interface ParentProps {
  title?: string | FormattedMessage.MessageDescriptor;
  titleType?: AboutSectionTitleType;
  message?: string | FormattedMessage.MessageDescriptor;
  messageValues?: {[key: string]: MessageValue | JSX.Element};
  style?: React.CSSProperties;
}

export const AboutSection: React.SFC<ParentProps> = (props) => {
  const { title, titleType, message, messageValues, style, children } = props;

  let heading;
  if (title) {
    if (isIntlMessageDescriptor(title)) {
      if (titleType) {
        const titleHighlight = (
          <span style={{ color: themeColors.primaryBlue }}>
            <FormattedMessage {...getTitleHighlightMessage(titleType)}/>
          </span>
        );

        heading = <FormattedMessage {...title} values={{ titleHighlight }}/>;
      } else {
        heading = <FormattedMessage {...title}/>;
      }
    } else {
      heading = title;
    }
  }

  return (
    <AboutSectionWrapper column={true} style={style}>
      {heading && <Heading size={3}>{heading}</Heading>}

      {message && <AboutSectionParagraph message={message} values={messageValues} />}

      {children}
    </AboutSectionWrapper>
  );
};

function getTitleHighlightMessage (titleType: AboutSectionTitleType) {
  // jw: this function should only ever be called if we have a titleType.
  if (!titleType) {
    throw new Error('getTitleHighlightMessage: titleType not provided');
  }

  return HighlightTitleTextTypes[titleType];
}
