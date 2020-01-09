import * as React from 'react';
import { compose, withProps } from 'recompose';
import * as moment from 'moment-timezone';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from '../containers/withExtractedCurrentUser';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import 'moment/min/locales';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';

// Props that come from the parent or user of this component
interface ParentProps {
  time: string;
  dateOnly?: boolean;
  fromNow?: boolean;
  itemProp?: string;
}

// Props that come from the composition withProps
type Props =
  ParentProps &
  {
    locale: string;
    timeZone: string;
    tokenValid: boolean;
  };

// The component view
const LocalizedTimeComponent: React.SFC<Props> = (props) => {
  const { time, locale, timeZone, dateOnly, fromNow, itemProp } = props;

  const date = moment(time).locale(locale).tz(timeZone);

  if (fromNow) {
    return <span itemProp={itemProp}>{date.fromNow()}</span>;
  }

  if (dateOnly) {
    return <span itemProp={itemProp}>{date.format('LL')}</span>;
  }

  return <span itemProp={itemProp}>{date.format('ll [@] h:mma')}</span>;
};

// Do the work to build the props that the Component needs
export const LocalizedTime = compose(
  injectIntl,
  withExtractedCurrentUser,
  withProps((props: WithExtractedCurrentUserProps & InjectedIntlProps) => {
    const { currentUserFormatPreferences, intl } = props;

    // Get locale from the user's prefs, default to en_US
    const locale = (currentUserFormatPreferences)
      ? currentUserFormatPreferences.locale
      : intl.formatMessage(SharedComponentMessages.DefaultLocalForDateFormat);

    // Get the timezone from the user's prefs if available. If not, guess
    const timeZone = (currentUserFormatPreferences) ? currentUserFormatPreferences.timeZone : moment.tz.guess();

    return { locale, timeZone };
  })
)(LocalizedTimeComponent) as React.ComponentClass<ParentProps>;
