import gql from 'graphql-tag';

export const NeoWalletFragment = gql`
  fragment NeoWallet on NeoWallet {
    oid
    type
    neoAddress
    extraNeoAddress
    scriptHash
    monthForDisplay
  }
`;
