import gql from 'graphql-tag';

export const shutdownNoticeUrlQuery = gql`
  query ShutdownNoticeUrlQuery {
    getShutdownNoticeUrl @rest(type: "ShutdownNoticeUrl", path: "/config/notice-url") {
      shutdownNoticeUrl: value
    }
  }
`;
