import * as React from 'react';
import { compose, withProps } from 'recompose';
import { withUsdFromNrve, WithUsdFromNrveProps } from '@narrative/shared';
import { Text } from './Text';
import { FormattedMessage } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';

interface ParentProps {
  nrve: string;
  nrveUsdPrice: string;
  showUsdOnly?: boolean;
}

type Props = WithUsdFromNrveProps & ParentProps;

const UsdFromNrveComponent: React.SFC<Props> = (props) => {
  const { usdAmount, loading, showUsdOnly } = props;

  if (loading || !usdAmount) {
    return null;
  }

  return (
    <Text color="light">
      <FormattedMessage
        {...(showUsdOnly ? SharedComponentMessages.UsdAmount : SharedComponentMessages.ApproximatelyUsdAmount)}
        values={{usdAmount}}
      />
    </Text>
  );
};

export const UsdFromNrve = compose(
  withProps((props: ParentProps) => {
    const { nrveUsdPrice } = props;
    const nrveAmount = props.nrve;

    return { input: { nrveAmount, nrveUsdPrice } };
  }),
  withUsdFromNrve
)(UsdFromNrveComponent) as React.ComponentClass<ParentProps>;
