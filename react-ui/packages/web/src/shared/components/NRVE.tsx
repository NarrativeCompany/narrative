import * as React from 'react';
import { Link } from './Link';

interface Props {
  amount?: number | string | null;
  minFractionLength?: number;
  maxFractionLength?: number;
  aboutNrveTarget?: string;
}

export const NRVE: React.SFC<Props> = (props) => {
  const { amount, minFractionLength, maxFractionLength, aboutNrveTarget } = props;

  let nrveAmount: string;
  // jw: if we were given a string, let's assume it already has all necessary numeric formatting applied to it.
  if (typeof amount === 'string') {
    nrveAmount = amount;

  } else {
    // jw: otherwise, let's get a number from the input, and format that with the specified criteria.
    const value = amount || 0;

    // jw: we want to ensure that we use the US format for numbers when rendering NRVE
    nrveAmount = value.toLocaleString('en-US', {
      minimumFractionDigits: minFractionLength,
      maximumFractionDigits: maxFractionLength
    });
  }

  return (
    <React.Fragment>
      {nrveAmount}&nbsp;
      <Link.About type="nrve" style={{ fontSize: 'inherit' }} target={aboutNrveTarget}/>
    </React.Fragment>
  );
};
