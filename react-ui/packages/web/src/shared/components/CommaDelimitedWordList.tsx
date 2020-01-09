import * as React from 'react';
import { compose } from 'recompose';
import { MessageValue } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';

type MessageDescriptor = FormattedMessage.MessageDescriptor;

// jw: I am exporting a function for places where we might need this during a HOC stack, and need the string for a
//     message instead of a node.
export function createCommaDelimitedWordList(
  wordList: string[],
  formatMessage: (messageDescriptor: MessageDescriptor, values?: {[key: string]: MessageValue}) => string
): string {
  if (!wordList || !wordList.length) {
    return '';

  } else if (wordList.length === 1) {
    return wordList[0];
  }

  // jw: only include commas if we have more than two words in the list.
  const includeCommas = wordList.length > 2;

  // jw: I had initially used a reduce to build this, but it was changing the order of the words.
  let result = '';
  wordList.forEach((word, index) => {
    // jw: if this is the first word short out.
    if (result === '') {
      result = word;
      return;
    }

    if (includeCommas) {
      result += ',';
    }

    // jw: at this point we always need to include and on the last element.
    const isLast = index === (wordList.length - 1);
    if (isLast) {
      result += ' ' + formatMessage(SharedComponentMessages.And);
    }

    result += ' ' + word;
  });

  return result;
}

/**
 * With the above out of the way, I am going to include this handy dandy component to make life easy when you need this
 * inside of a component.
 */

interface ParentProps {
  wordList: string[];
}

type Props = ParentProps &
  InjectedIntlProps;

const CommaDelimitedWordListComponent: React.SFC<Props> = (props) => {
  const { wordList, intl: { formatMessage } } = props;
  return (
    <React.Fragment>
      {createCommaDelimitedWordList(wordList, formatMessage)}
    </React.Fragment>
  );
};

export const CommaDelimitedWordList = compose(
  injectIntl
)(CommaDelimitedWordListComponent) as React.ComponentClass<ParentProps>;
