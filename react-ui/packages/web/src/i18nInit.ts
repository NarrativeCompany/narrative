import { IntlProvider, addLocaleData } from 'react-intl';
import * as es from 'react-intl/locale-data/es';
addLocaleData(es);

// TODO: Conditionally init locale and messages based on environment

const locale = process.env.REACT_APP_LOCALE || 'en';
const messages = (currentLocale?: string) => {
  switch (currentLocale) {
    case 'es':
      return require('./shared/i18n/translations/es.json');
    default:
      return {};
  }
};

export const intlProviderConfig: IntlProvider.Props = {
  locale,
  messages: messages(locale)
};
