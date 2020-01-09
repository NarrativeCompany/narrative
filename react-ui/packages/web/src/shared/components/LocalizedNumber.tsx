import * as React from 'react';
import { compose, withProps } from 'recompose';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from '../containers/withExtractedCurrentUser';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';

interface ParentProps {
  value?: number | null;
  minFractionLength?: number;
  maxFractionLength?: number;
}

type Props = ParentProps &
  {
    locale: string;
  };

const LocalizedNumberComponent: React.SFC<Props> = (props) => {
  const { locale, minFractionLength, maxFractionLength } = props;
  let value = props.value;

  // jw: using a loose equality, so that undefined will match
  if (value == null) {
    value = 0;
  }

  // jw: return the formatted number
  return (
    <React.Fragment>
      {value.toLocaleString(locale, {
        minimumFractionDigits: minFractionLength,
        maximumFractionDigits: maxFractionLength
      })}
    </React.Fragment>
  );
};

export const LocalizedNumber = compose(
  injectIntl,
  withExtractedCurrentUser,
  withProps((props: WithExtractedCurrentUserProps & InjectedIntlProps) => {
    const { currentUserFormatPreferences, intl } = props;

    // Get language from the user's prefs default to en-US
    const locale = currentUserFormatPreferences
      // jw: numbers expect a ISO BCP 47 formatted locale here.
      ? currentUserFormatPreferences.localeForNumber
      : intl.formatMessage(SharedComponentMessages.DefaultLocaleForNumberFormat);

    return { locale };
  })
)(LocalizedNumberComponent) as React.ComponentClass<ParentProps>;
