import * as React from 'react';

interface Props {
  value?: number | string | null;
}

export const USD: React.SFC<Props> = (props) => {
  let value = props.value;

  // jw: if the value provided was a string, let's assume it was pre-formatted for us.
  if (typeof value === 'string') {
    return (
      <React.Fragment>
        {value}
      </React.Fragment>
    );
  }

  // jw: using a loose equality, so that undefined will match
  if (value == null) {
    value = 0;
  }

  // jw: return the formatted number
  return (
    <React.Fragment>
      {value.toLocaleString('en-US', {style: 'currency', currency: 'USD'})}
    </React.Fragment>
  );
};
