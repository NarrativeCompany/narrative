import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import { RouteComponentProps } from 'react-router';
import { View, StyleSheet, ScrollView } from 'react-native';
import { SuggestNiche } from '../SuggestNiche/SuggestNiche';
import { PageHeader } from '../../shared/components/PageHeader';
import { ActivityIndicator, Button, List, Tag, WhiteSpace } from 'antd-mobile-rn';
import {
  withAllNichesOptions,
  WithAllNichesProps,
  Niche,
  ApiOpts,
  withState,
  WithStateProps
} from '@narrative/shared';
import { AppRoutes } from '../../shared/constants/routes';

interface State {
  isSuggestNicheFormVisible: boolean;
}
const initialState: State = {
  isSuggestNicheFormVisible: false,
};

interface WithProps {
  niches: Niche[]
}

type Props =
  WithAllNichesProps &
  WithProps &
  WithStateProps<State> &
  RouteComponentProps<{}>;

const BallotBoxComponent: React.SFC<Props> = (props) => {
  const { niches, setState, state } = props;

  return (
    <ScrollView
      style={{ flex: 1 }}
      automaticallyAdjustContentInsets={false}
      showsHorizontalScrollIndicator={false}
      showsVerticalScrollIndicator={false}
      contentContainerStyle={styles.container}
    >

      <PageHeader title="Niches">
        <Button
          type="primary"
          size="small"
          onClick={() => props.history.push(AppRoutes.SuggestNiche)}
        >
          Suggest Niche
        </Button>
      </PageHeader>

      <WhiteSpace size="lg"/>

      {niches.length &&
      <List>
        {niches.map((niche: Niche) => (
          <List.Item
            key={niche.id}
            extra={(<Tag small={true}>{niche.status}</Tag>)}
            align="top"
            multipleLine
          >
            {niche.name}
            <List.Item.Brief>{niche.description}</List.Item.Brief>
          </List.Item>
        ))}
      </List>}

      {state.isSuggestNicheFormVisible &&
      <SuggestNiche dismiss={() => setState(ss => ({...ss, isSuggestNicheFormVisible: false}))}/>}
    </ScrollView>
  );
};

export const BallotBox = compose<Props, {}>(
  withAllNichesOptions(ApiOpts.REST),
  withState<State>(initialState),
  withProps((props: Props) => {
    const { allNichesData } = props;
    const niches =
      allNichesData &&
      allNichesData.getAllNiches || [];

    return { niches };
  }),
  branch((props: Props) => props.allNichesData.loading,
    renderComponent(() => (
      <View style={styles.loadingWrapper}><ActivityIndicator text="loading" color="cornflowerblue"/></View>
    ))
  )
)(BallotBoxComponent) as React.ComponentClass<{}>;

const styles = StyleSheet.create({
  container: {
    paddingTop: 20,
    paddingBottom: 20
  },
  headerWrapper: {
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 10,
    paddingBottom: 10
  },
  headerText: {
    fontSize: 24,
    fontWeight: '600'
  },
  loadingWrapper: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center'
  }
});
