import * as React from 'react';
import { branch, compose, renderComponent } from 'recompose';
import { Form, withFormik, FormikProps } from 'formik';
import { injectIntl, InjectedIntlProps, FormattedMessage } from 'react-intl';
import { Heading } from '../Heading';
import { FormField } from '../FormField';
import { Button } from '../Button';
import { FormMethodError } from '../FormMethodError';
import { FlexContainer } from '../../styled/shared/containers';
import { CommentMessages } from '../../i18n/CommentMessages';
import { WithNewCommentHandler } from './CommentsSection';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from '../../containers/withExtractedCurrentUser';
import {
  withState,
  WithStateProps,
  withPostComment,
  WithPostCommentProps,
  PostCommentFormValues,
  commentFormUtil,
  applyExceptionToState,
  MethodError,
  initialFormState
} from '@narrative/shared';
import { MemberAvatar } from '../user/MemberAvatar';
import styled from '../../styled';
import { CommentsSectionProps } from './CommentsSection';

const AvatarWrapper = styled(FlexContainer)`
  width: 60px;
  
  img {
    max-width: 40px;
  }
`;

type ParentProps =
  CommentsSectionProps &
  WithNewCommentHandler;

type WithFormikProps =
  ParentProps &
  WithPostCommentProps &
  WithStateProps<MethodError> &
  FormikProps<PostCommentFormValues>;

type Props =
  ParentProps &
  WithExtractedCurrentUserProps &
  InjectedIntlProps &
  WithStateProps<MethodError> &
  FormikProps<PostCommentFormValues>;

const CommentingFormComponent: React.SFC<Props> = (props) => {
  const { currentUser, intl: { formatMessage }, state, isSubmitting } = props;

  if (!currentUser) {
    return null;
  }

  return (
    <React.Fragment>
      <Heading size={6} uppercase={true} style={{marginBottom: 15}}>
        <FormattedMessage {...CommentMessages.AddComment} />
      </Heading>
      <Form>
        <FlexContainer alignItems="flex-start" style={{marginBottom: 40}}>
          <AvatarWrapper justifyContent="center">
            <MemberAvatar user={currentUser} />
          </AvatarWrapper>

          <FlexContainer column={true} style={{width: '100%'}}>
            <FormMethodError methodError={state.methodError}/>

            <FormField.TextArea
              placeholder={formatMessage(CommentMessages.WriteAComment)}
              name="body"
              style={{marginBottom: 15}}
              rows={4}
            />
            <Button type="primary" htmlType="submit" style={{maxWidth: 150}} loading={isSubmitting}>
              <FormattedMessage {...CommentMessages.PostComment}/>
            </Button>
          </FlexContainer>
        </FlexContainer>
      </Form>
    </React.Fragment>
  );
};

export const CommentingForm = compose(
  // jw: If the member cannot comment or like short out
  branch((props: CommentsSectionProps) => (!props.canCommentOrRate),
    renderComponent(() => null)
  ),
  withPostComment,
  withState<MethodError>(initialFormState),
  withFormik<WithFormikProps, PostCommentFormValues>({
    ...commentFormUtil,
    mapPropsToValues: () => {
      return { body: '' };
    },
    handleSubmit: async (values, {props, setErrors, resetForm, setSubmitting}) => {
      const { isSubmitting, setState, postComment, handleNewComment, consumerType, consumerOid } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        // jw: let's submit the comment to the server.
        const { body } = values;
        await postComment({ body }, { consumerType, consumerOid });

        // jw: assuming that a handler was passed in, let's notify our parent about the new comment.
        if (handleNewComment) {
          handleNewComment();
        }
        // jw: Now that the comment has been posted and the UI is updating, let's reset the form.
        resetForm();

      } catch (err) {
        applyExceptionToState(err, setErrors, setState);
      }

      setSubmitting(false);
    }
  }),
  injectIntl,
  withExtractedCurrentUser
)(CommentingFormComponent) as React.ComponentClass<ParentProps>;
