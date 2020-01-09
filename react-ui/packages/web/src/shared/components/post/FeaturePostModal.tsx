import * as React from 'react';
import { compose } from 'recompose';
import {
  applyExceptionToState,
  FeaturePostDuration,
  initialFormState,
  MethodError,
  SimpleFormState,
  withState,
  WithStateProps,
  featurePostFormikUtil,
  FeaturePostFormValues
} from '@narrative/shared';
import { SharedFeaturePostModalsProps } from './FeaturePostModals';
import { Form, FormikProps, withFormik } from 'formik';
import { Select } from 'antd';
import { PublicationDetailsMessages } from '../../i18n/PublicationDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { FormMethodError } from '../FormMethodError';
import { ConfirmationModal } from '../ConfirmationModal';
import { Paragraph } from '../Paragraph';
import { Block } from '../Block';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { FormControl } from '../FormControl';
import { SelectValue } from 'antd/lib/select';
import { EnhancedFeaturePostDuration } from '../../enhancedEnums/featurePostDuration';

export interface FeaturePostModalHandler {
  featurePostHandler?: (duration: FeaturePostDuration) => void;
}

type ParentProps = FeaturePostModalHandler & SharedFeaturePostModalsProps;

type Props =
  ParentProps &
  WithStateProps<MethodError> &
  FormikProps<FeaturePostFormValues>;

const FeaturePostModalComponent: React.SFC<Props> = (props) => {
  const { featurePostHandler, post } = props;

  const {
    submitForm,
    setFieldValue,
    processing,
    closeModalHandler,
    state: { methodError },
    values: { duration }
  } = props;

  return (
    <ConfirmationModal
      // jw: only visible if we have a post and we have a featurePostHandler.
      visible={!!post && !!featurePostHandler}
      processing={processing}
      dismiss={closeModalHandler}
      // jw: I kinda hate this, but since the buttons are outside of the form we have to manually trigger the submitForm
      onConfirmation={submitForm}
      title={PublicationDetailsMessages.FeaturePostTitle}
      btnText={PublicationDetailsMessages.FeaturePostButtonText}
      btnProps={{ type: 'primary' }}
      linkText={SharedComponentMessages.Cancel}
      linkProps={{ onClick: closeModalHandler }}
    >
      {/*
        jw: not 100% sure why, but putting this block element around the body causes everything to get a consistent
            max-width applied to it. Without this the form will extend much wider than the description.
       */}
      <Block style={{maxWidth: 450}}>
        <Paragraph marginBottom="large">
          <FormattedMessage {...PublicationDetailsMessages.FeaturePostQuestion} />
        </Paragraph>
        <Form>
          <FormMethodError methodError={methodError}/>

          <FormControl
            label={<FormattedMessage {...PublicationDetailsMessages.FeatureDurationLabel}/>}
          >
            <Select
              size="large"
              value={duration}
              onChange={(value: SelectValue) => setFieldValue('duration', value)}
            >
              {EnhancedFeaturePostDuration.enhancers.map((helper) =>
                <Select.Option key={`duration_${helper.duration}`} value={helper.duration}>
                  <FormattedMessage {...helper.title} />
                </Select.Option>
              )}
            </Select>
          </FormControl>
        </Form>
      </Block>
    </ConfirmationModal>
  );
};

export const FeaturePostModal = compose(
  withState<SimpleFormState>(initialFormState),
  withFormik<Props, FeaturePostFormValues>({
    ...featurePostFormikUtil,
    handleSubmit: async (values, {props, setErrors, resetForm, setSubmitting}) => {
      const { isSubmitting, setState, featurePostHandler } = props;

      if (!featurePostHandler) {
        // todo:error-handling: We should always have a featurePostHandler if we got into here!
        return;
      }

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        // jw: let's extract the duration and provide it to the handler for processing.
        const { duration } = values;

        await featurePostHandler(duration);

        // jw: Now that the duration has been applied and the UI is updating, let's reset the form.
        resetForm();

      } catch (err) {
        applyExceptionToState(err, setErrors, setState);
      }

      setSubmitting(false);
    }
  }),
  )(FeaturePostModalComponent) as React.ComponentClass<ParentProps>;
