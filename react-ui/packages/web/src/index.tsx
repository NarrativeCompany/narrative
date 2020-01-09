import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { BrowserRouter as Router } from 'react-router-dom';
import { ApolloProvider } from 'react-apollo';
import { apolloClient, initializeAuthState } from './apolloClientInit';
import { theme, ThemeProvider } from './shared/styled';
import { injectGlobalStyles } from './shared/styled/global/global';
import { stores, UIState } from './shared/stores';
import { intlProviderConfig } from './i18nInit';
import { FormattedMessage, IntlProvider } from 'react-intl';
import { getState, loadComponents } from 'loadable-components';
import 'typeface-lato';
import { App } from './App';
import * as serviceWorker from './registerServiceWorker';
import { ErrorBoundary } from './shared/components/ErrorBoundary';
import { NotFound } from './shared/components/NotFound';
import { SharedComponentMessages } from './shared/i18n/SharedComponentMessages';
import * as moment from 'moment-timezone';

initializeAuthState();
injectGlobalStyles();

// tslint:disable no-string-literal
window['snapSaveState'] = () => getState();
// IMPORTANT: if you change the following line, you _must_ also update build-narrative-web-front-end.sh
// because it relies on sed to inject the version here. if this line changes, the build process will fail.
window['NARRATIVE_VERSION'] = 'local';
// tslint:enable no-string-literal

const rootElement = document.getElementById('root');

const AppIndex: React.SFC<{}> = () => {
  const generateError = () =>
    <NotFound message={<FormattedMessage {...SharedComponentMessages.DefaultErrorMessage}/>}/>;

  return (
    <IntlProvider locale={intlProviderConfig.locale} messages={intlProviderConfig.messages}>
      <ApolloProvider client={apolloClient}>
        <ErrorBoundary generateError={generateError}>
          <Router>
            <UIState store={stores}>
              <ThemeProvider theme={theme}>
                <App/>
              </ThemeProvider>
            </UIState>
          </Router>
        </ErrorBoundary>
      </ApolloProvider>
    </IntlProvider>
  );
};

moment.relativeTimeThreshold('ss', 3);

if (rootElement && rootElement.hasChildNodes()) {
  loadComponents().then(() => {
    ReactDOM.hydrate(<AppIndex/>, rootElement);
  });
} else {
  ReactDOM.render(<AppIndex/>, rootElement);
}
serviceWorker.unregister();
