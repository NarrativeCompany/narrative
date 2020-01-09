import * as React from 'react';
import { Card } from '../Card';

// jw: I can hear frank in the back of my head saying, why are you creating this, and for the first time I am
//     note sure. I like the fact that it centralizes the presentation for the action placeholder, allowing us to
//     ensure that we are rendering these consistently across the various actions that will be included on detail
//     pages. Though, this will likely only be used for the NicheDetails, where we need to load the actions via
//     ajax.
export const DetailsActionPlaceholderCard: React.SFC<{}> = () => {
  return <Card loading={true} style={{marginBottom: '20px'}} />;
};
