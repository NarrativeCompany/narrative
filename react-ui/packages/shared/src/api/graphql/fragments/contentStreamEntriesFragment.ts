import gql from 'graphql-tag';
import { PostFragment } from './postFragment';
import { ContentStreamScrollParamsFragment } from './contentStreamScrollParamsFragment';

export const ContentStreamEntriesFragment = gql`
  fragment ContentStreamEntries on ContentStreamEntries {
    items @type(name: "Post") {
      ...Post
    }
    hasMoreItems
    scrollParams @type(name: "ContentStreamScrollParams") {
      ...ContentStreamScrollParams
    }
  }
  ${PostFragment}
  ${ContentStreamScrollParamsFragment}
`;
