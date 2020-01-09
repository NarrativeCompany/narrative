import * as React from 'react';
import { ApolloProvider } from 'react-apollo';
import { apolloClient as client } from '@narrative/shared';
import { NativeRouter, Switch, Route } from 'react-router-native';
import { AuthRoute } from './shared/components/AuthRoute';
import { Register } from "./routes/Register/Register";
import { Login } from './routes/Login/Login';
import { BallotBox } from './routes/BallotBox/BallotBox';
import { SuggestNiche } from './routes/SuggestNiche/SuggestNiche';
import { AppRoutes } from './shared/constants/routes';
import { Search } from './routes/Search/Search';

export const App = () => (
  <ApolloProvider client={client}>
    <NativeRouter>
      <Switch>
        <Route exact={true} path={AppRoutes.Login} component={Login}/>
        <Route exact={true} path={AppRoutes.Register} component={Register}/>
        <Route exact={true} path={AppRoutes.BallotBox} component={BallotBox}/>
        <Route exact={true} path={AppRoutes.Search} component={Search}/>
        <AuthRoute exact={true} path={AppRoutes.SuggestNiche} component={SuggestNiche}/>
      </Switch>
    </NativeRouter>
  </ApolloProvider>
);
