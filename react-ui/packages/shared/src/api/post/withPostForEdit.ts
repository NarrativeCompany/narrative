import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { postForEditQuery } from '../graphql/post/postForEditQuery';
import {
  AgeRating,
  Niche,
  PostForEditQuery,
  PostInput,
  Publication,
  PublicationDetail,
  EditPostDetail
} from '../../types';

const queryName = 'postForEditData';

interface ParentProps {
  postOid?: string;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & PostForEditQuery},
  WithPostForEditProps
>;

export type WithPostForEditProps =
  ChildDataProps<ParentProps, PostForEditQuery> & {
  submitPostInitialValues?: PostInput;
  postForEditLoading: boolean;
  initialSelectedNiches: Niche[];
  selectedPublicationDetail?: PublicationDetail;
  postNotFound: boolean;
  blockedInNicheOids: string[];
  availablePublications: Publication[];
  authorPersonalJournalOid: string;
  editPostDetail?: EditPostDetail;
};

export const withPostForEdit =
  graphql<
    ParentProps,
    PostForEditQuery,
    {},
    WithPostForEditProps
  >(postForEditQuery, {
    skip: ({ postOid }: ParentProps) => !postOid,
    options: ({ postOid }: ParentProps) => ({
      variables: {
        postOid
      }
    }),
    name: queryName,
    props: ({ postForEditData, ownProps }: WithProps): WithPostForEditProps => {
      const { loading, getPostForEdit } = postForEditData;
      let postNotFound = false;

      // check for 404
      if (!loading && ownProps.postOid && getPostForEdit === null) {
        postNotFound = true;
      }
      const postForEditLoading = loading;
      const submitPostInitialValues = mapResponseToSubmitPostInputDTO(postForEditData);
      const initialSelectedNiches = getInitialSelectedNiches(postForEditData) as Niche[];
      const blockedInNicheOids = getBlockedInNicheOids(postForEditData);
      const availablePublications = getAvailablePublications(postForEditData);

      const authorPersonalJournalOid = getPostForEdit && getPostForEdit.authorPersonalJournalOid;
      const selectedPublicationDetail = getPostForEdit && getPostForEdit.publishedToPublicationDetail || undefined;

      return {
        ...ownProps,
        submitPostInitialValues,
        postForEditLoading,
        initialSelectedNiches,
        postNotFound,
        blockedInNicheOids,
        availablePublications,
        authorPersonalJournalOid,
        selectedPublicationDetail,
        editPostDetail: getPostForEdit
      };
    }
  });

function getInitialSelectedNiches (data: GraphqlQueryControls & PostForEditQuery) {
  const { getPostForEdit } = data;

  if (!getPostForEdit) {
    return [];
  }

  return getPostForEdit.postDetail.post.publishedToNiches || [];
}

function getBlockedInNicheOids(data: PostForEditQuery): string[] {
  const { getPostForEdit } = data;

  if (!getPostForEdit) {
    return [];
  }

  return getPostForEdit.blockedInNicheOids;
}

function getAvailablePublications(data: PostForEditQuery): Publication[] {
  const { getPostForEdit } = data;

  if (!getPostForEdit) {
    return [];
  }
  return getPostForEdit.availablePublications;
}

function mapResponseToSubmitPostInputDTO (data: GraphqlQueryControls & PostForEditQuery): PostInput | undefined {
  const { getPostForEdit } = data;

  if (!getPostForEdit) {
    return;
  }

  const postDetail = getPostForEdit.postDetail;
  const post = postDetail.post;
  // bl: set ageRestricted to undefined if we don't have a value, which means it's not editable on the form
  const ageRestricted = getPostForEdit.authorAgeRating
    ? getPostForEdit.authorAgeRating === AgeRating.RESTRICTED
    : null;
  const publishToNiches = getInitialSelectedNiches(data).map((niche: Niche) => niche && niche.oid);

  let publishToPrimaryChannel: string | undefined;

  if (post.publishedToPublication) {
    publishToPrimaryChannel = post.publishedToPublication.oid;
  } else if (post.publishedToPersonalJournal) {
    publishToPrimaryChannel = getPostForEdit.authorPersonalJournalOid;
  } else if (post.publishedToNiches && post.publishedToNiches.length > 0) {
    // zb: we will set the publishToPrimaryChannel to the 'None' option which is an empty string if
    // it is null at this point at the user had selected some niches, since that is the only
    // possible option for the primary channel
    publishToPrimaryChannel = '';
  }

  return {
    draft: postDetail.draft,
    title: post.title,
    subTitle: post.subTitle,
    body: getPostForEdit.rawBody,
    canonicalUrl: postDetail.canonicalUrl,
    disableComments: !postDetail.allowComments,
    ageRestricted,
    publishToNiches,
    publishToPrimaryChannel
  };
}
