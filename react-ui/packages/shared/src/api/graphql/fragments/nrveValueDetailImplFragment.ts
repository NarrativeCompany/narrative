import gql from 'graphql-tag';

export const NrveValueDetailImplFragment = gql`
  fragment NrveValueDetailImpl on NrveValueDetailImpl {
    nrve
    nrveRounded
    nrveDecimal
  }
`;
