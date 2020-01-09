import gql from 'graphql-tag';

export const PublicationUrlsFragment = gql`
  fragment PublicationUrls on PublicationUrls {
    websiteUrl
    twitterUrl
    facebookUrl
    instagramUrl
    youtubeUrl
    snapchatUrl
    pinterestUrl
    linkedInUrl
  }
`;
