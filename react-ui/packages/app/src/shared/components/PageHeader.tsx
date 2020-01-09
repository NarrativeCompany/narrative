import * as React from 'react';
import { WingBlank } from 'antd-mobile-rn';
import { StyleSheet, View, Text } from 'react-native';

interface Props {
  title: string;
  children?: React.ReactNode;
}

export const PageHeader: React.SFC<Props> = (props) => {
  return (
    <WingBlank>
      <View style={styles.headerWrapper}>
        <Text style={styles.headerText}>{props.title}</Text>

        {props.children}
      </View>
    </WingBlank>
  );
};

const styles = StyleSheet.create({
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
  }
});