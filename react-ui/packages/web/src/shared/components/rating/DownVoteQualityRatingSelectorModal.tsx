import * as React from 'react';
import { compose } from 'recompose';
import {
  downVoteQualityRatingFormikUtil,
  DownVoteQualityRatingFormValues,
  initialFormState,
  MethodError,
  QualityRating,
  withState,
  WithStateProps
} from '@narrative/shared';
import { Form, FormikProps, withFormik } from 'formik';
import { showValidationErrorDialogIfNecessary } from '../../utils/webErrorUtils';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { EnhancedQualityRating } from '../../enhancedEnums/qualityRating';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { RadioField, RadioGroupField } from '../RadioGroupField';
import { Button } from '../Button';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { Link } from '../Link';
import { Modal } from 'antd';
import { RatingMessages } from '../../i18n/RatingMessages';
import { FormMethodError } from '../FormMethodError';
import styled from 'styled-components';
import { Heading } from '../Heading';
import { InputField } from '../InputField';

export interface DownVoteQualityRatingSelectorModalProps {
  visible: boolean;
  dismiss: () => void;
  onDownVoteRatingSelected: (rating: QualityRating, reason?: string) => void;
  currentRating?: QualityRating;
}

const ModalButtons = styled<FlexContainerProps>(FlexContainer)`
  margin: 25px auto auto;
  width: 150px;
  & > a {
    margin-top: 15px;
  }
`;

type Props =
  DownVoteQualityRatingSelectorModalProps &
  WithStateProps<MethodError> &
  InjectedIntlProps &
  FormikProps<DownVoteQualityRatingFormValues>;

type WithFormikProps =
  Props &
  FormikProps<DownVoteQualityRatingFormValues>;

const DownVoteQualityRatingSelectorModalComponent: React.SFC<Props> = (props) => {
  const { dismiss, isSubmitting, visible, intl, setState, values, resetForm } = props;

  const aupLink = <Link.Legal type="aup" />;

  // jw: let's setup all of the radio options from the dislike reasons
  const radioFields: RadioField = EnhancedQualityRating.enhancers
    .filter((rating) => rating.isDislikeReason())
    .map((rating) => ({
      value: rating.rating,
      text: <FormattedMessage {...rating.titleMessage} values={{aupLink}}/>
    }));

  const aupReasons = (
    <ul>
      <li><FormattedMessage {...RatingMessages.NotWrittenInEnglish} /></li>
      <li><FormattedMessage {...RatingMessages.CopyrightInfringement} /></li>
      <li><FormattedMessage {...RatingMessages.IllegalActivities} /></li>
      <li><FormattedMessage {...RatingMessages.Pornography} /></li>
    </ul>
  );

  const afterClose = () => {
    resetForm();
    setState(initialFormState);
  };

  return (
    <Modal
      visible={visible}
      footer={null}
      destroyOnClose={true}
      afterClose={afterClose}
      onCancel={dismiss}
      width={450}
    >
      <Heading size={3}><FormattedMessage {...RatingMessages.ReasonForDownvote} /></Heading>
      <Form>
        <FormMethodError methodError={props.state.methodError}/>

        <RadioGroupField
          name="rating"
          fieldMargin={0}
          radioFields={radioFields}
          // jw: this is a bit of a cheat, but since the AUP reason is last, we can just append the reason list to the
          //     end of the RadioGroup through `help` and call it a day... Feel's just a touch dirty, but it works.
          help={aupReasons}
        />

        {values.rating && values.rating === QualityRating.DISLIKE_CONTENT_VIOLATES_AUP &&
          <InputField
            name="reason"
            placeholder={intl.formatMessage(RatingMessages.Reason)}
          />
        }

        <ModalButtons direction="column" alignItems="center">
          <Button type="danger" block={true} htmlType="submit" loading={isSubmitting}>
            <FormattedMessage {...RatingMessages.Vote}/>
          </Button>

          <Link.Anchor onClick={dismiss} color="light">
            <FormattedMessage {...SharedComponentMessages.Cancel}/>
          </Link.Anchor>
        </ModalButtons>
      </Form>
    </Modal>
  );
};

export const DownVoteQualityRatingSelectorModal = compose(
  injectIntl,
  withState<MethodError>(initialFormState),
  withFormik<WithFormikProps, DownVoteQualityRatingFormValues>({
    validationSchema: downVoteQualityRatingFormikUtil.validationSchema,
    mapPropsToValues: (props: DownVoteQualityRatingSelectorModalProps) => {
      const { currentRating } = props;

      const enhancedRating = currentRating && EnhancedQualityRating.get(currentRating);

      let rating;
      if (enhancedRating && enhancedRating.isDislikeReason()) {
        rating = enhancedRating.rating;
      }

      return downVoteQualityRatingFormikUtil.mapPropsToValues({rating});
    },
    handleSubmit: async (values, {props, setSubmitting}) => {
      const { onDownVoteRatingSelected, intl, isSubmitting } = props;

      if (isSubmitting) {
        return;
      }

      try {
        if (!values.rating) {
          // todo:error-handling: This should never happen since the form should validate we have a rating.
          return;
        }
        await onDownVoteRatingSelected(values.rating, values.reason);

      } catch (err) {
        showValidationErrorDialogIfNecessary(intl.formatMessage(SharedComponentMessages.FormErrorTitle), err);

      } finally {
        setSubmitting(false);
        props.dismiss();
      }
    }
  })
)(DownVoteQualityRatingSelectorModalComponent) as React.ComponentClass<DownVoteQualityRatingSelectorModalProps>;
