import * as React from 'react';
import { Paragraph } from '../../../shared/components/Paragraph';

export const PostBulletedDetail: React.SFC<{}> = (props) => {
  return (
    <React.Fragment>
      <Paragraph color="light" style={{ margin: '0 10px'}}>•</Paragraph>

      {props.children}
    </React.Fragment>
  );
};
