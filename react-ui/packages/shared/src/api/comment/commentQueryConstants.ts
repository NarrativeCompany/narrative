import { CommentQueryFieldsInput, CompositionConsumerQueryFieldsInput, CompositionConsumerType } from '../../types';

export interface WithCommentPostingProps {
  body: string;
}

export interface WithCompositionConsumerFields {
  consumerType: CompositionConsumerType;
  consumerOid: string;
}

export interface WithCommentQueryProps extends WithCompositionConsumerFields {
  commentOid: string;
}

export function getCompositionConsumerFields (props: WithCompositionConsumerFields):
  CompositionConsumerQueryFieldsInput
{
  const { consumerType, consumerOid } = props;

  return { consumerType, consumerOid };
}

export function getCommentFields(props: WithCommentQueryProps): CommentQueryFieldsInput {
  const { consumerType, consumerOid, commentOid } = props;

  return { consumerType, consumerOid, commentOid };
}
