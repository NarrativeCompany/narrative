import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import { Form, FormikProps, withFormik } from 'formik';
import { injectIntl, InjectedIntlProps, FormattedMessage } from 'react-intl';
import { Card } from '../Card';
import { FormMethodError } from '../FormMethodError';
import { Col, Row } from 'antd';
import { FormField } from '../FormField';
import { Button } from '../Button';
import { CommentMessages } from '../../i18n/CommentMessages';
import {
  withState,
  WithStateProps,
  PostCommentFormValues,
  commentFormUtil,
  applyExceptionToState,
  withEditComment,
  WithEditCommentProps,
  withCommentForEdit,
  WithCommentForEditProps,
  MethodError,
  initialFormState
} from '@narrative/shared';
import {
  CommentProps,
  WithToggleCommentEditFormHandler
} from './DisplayComment';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';

type ParentProps =
  CommentProps &
  WithToggleCommentEditFormHandler;

type WithFormikProps =
  CommentProps &
  WithToggleCommentEditFormHandler &
  WithEditCommentProps &
  FormikProps<PostCommentFormValues> &
  WithCommentForEditProps &
  WithStateProps<MethodError>;

type Props =
  ParentProps &
  InjectedIntlProps &
  WithStateProps<MethodError> &
  FormikProps<PostCommentFormValues>;

const CommentEditingFormComponent: React.SFC<Props> = (props) => {
  const { toggleCommentEditForm, intl: { formatMessage }, state, isSubmitting } = props;

  return (
    <Form>
      <FormMethodError methodError={state.methodError}/>

      <FormField.TextArea
        placeholder={formatMessage(CommentMessages.WriteAComment)}
        name="body"
        style={{marginBottom: '10px'}}
        rows={4}
      />
      <Row gutter={24}>
        <Col span={12}>
          <Button type="primary" style={{width: '100%'}} htmlType="submit" loading={isSubmitting}>
            <FormattedMessage {...CommentMessages.Submit}/>
          </Button>
        </Col>
        <Col span={12} >
          <Button type="danger" style={{width: '100%'}} onClick={() => toggleCommentEditForm(false)}>
            <FormattedMessage {...SharedComponentMessages.Cancel}/>
          </Button>
        </Col>
      </Row>
    </Form>
  );
};

export const CommentEditingForm = compose(
  // jw: If the member cannot comment or rate, let's just short out!
  branch((props: CommentProps) => (!props.canCommentOrRate),
    renderComponent(() => null)
  ),
  // jw: now that we know the user has the rights to be here, let's load the body for edit
  withProps((props: CommentProps) => {
    const { comment } = props;

    return {
      commentOid: comment.oid
    };
  }),
  withCommentForEdit,
  // jw: while it's loading, lets just show the loading card.
  branch((props: WithCommentForEditProps) => (props.commentForEditData.loading),
    renderComponent(() => <Card loading={true}/>)
  ),
  // jw: now that we have the body for edit, let's set up for edit.
  withEditComment,
  withState<MethodError>(initialFormState),
  withFormik<WithFormikProps, PostCommentFormValues>({
    ...commentFormUtil,
    mapPropsToValues: (props: WithCommentForEditProps) => {
      return { body: props.commentForEditData.getCommentForEdit.value };
    },
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { consumerType, consumerOid, comment } = props;
      const { setState, editComment, toggleCommentEditForm, isSubmitting } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        const commentOid = comment.oid;
        const body = values.body;
        await editComment({body}, {consumerType, consumerOid, commentOid});

        if (toggleCommentEditForm) {
          toggleCommentEditForm(false);
        }

      } catch (err) {
        applyExceptionToState(err, setErrors, setState);
      }

      setSubmitting(false);
    }
  }),
  injectIntl
)(CommentEditingFormComponent) as React.ComponentClass<ParentProps>;
