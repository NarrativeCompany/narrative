import * as React from 'react';
import { NrveUsdValue, NrveValueDetailImpl } from '@narrative/shared';
import { Tooltip } from 'antd';
import { Text } from '../Text';

interface Props {
  value: NrveUsdValue | NrveValueDetailImpl;
  customTooltip?: React.ReactNode;
  showFullDecimal?: boolean;
}

export const NrveValue: React.SFC<Props> = (props) => {
  const { value, customTooltip, showFullDecimal } = props;

  // jw: there are a couple of scenarios where we can short out, but only if we do not have a custom tooltip
  if (!customTooltip) {
    // jw: if the value is zero, then just display zero.
    if (value.nrve === '0') {
      return <React.Fragment>0</React.Fragment>;
    }

    // jw: if the value does not contain any decimals, then just display the nrve value.
    if (value.nrve === value.nrveRounded) {
      return <React.Fragment>{value.nrve}</React.Fragment>;
    }
  }

  // jw: we need to determine which format to present in the UI
  let nrveToDisplay;
  if (showFullDecimal && value.nrveDecimal !== '0') {
    nrveToDisplay = (
      <React.Fragment>
        {value.nrveRounded}
        <Text color="inherit" style={{fontSize: '70%'}}>.{value.nrveDecimal}</Text>
      </React.Fragment>
    );
  } else {
    nrveToDisplay = <React.Fragment>{value.nrveRounded}</React.Fragment>;
  }

  // jw: finally, we need to resolve the tooltip to wrap the value.
  return (
    <Tooltip title={customTooltip ? customTooltip : value.nrve}>
      {nrveToDisplay}
    </Tooltip>
  );
};
