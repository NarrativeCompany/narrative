import * as React from 'react';
import { compose } from 'recompose';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import { FormField } from '../../../shared/components/FormField';
import { CreatePublicationMessages } from '../../../shared/i18n/CreatePublicationMessages';

interface ParentProps {
  useLabels?: boolean;
}

type Props = ParentProps &
  InjectedIntlProps;

const PublicationNameAndDescriptionFormFieldsComponent: React.SFC<Props> = (props) => {
  const { useLabels, intl: { formatMessage } } = props;

  return (
    <React.Fragment>
      <FormField.Input
        name="name"
        size="large"
        type="text"
        label={useLabels ? <FormattedMessage {...CreatePublicationMessages.PublicationTitlePlaceholder} /> : undefined}
        placeholder={useLabels ? undefined : formatMessage(CreatePublicationMessages.PublicationTitlePlaceholder)}
      />
      <FormField.Input
        name="description"
        size="large"
        type="text"
        label={useLabels ? <FormattedMessage {...CreatePublicationMessages.PublicationDescriptionLabel} /> : undefined}
        placeholder={useLabels ? undefined : formatMessage(CreatePublicationMessages.PublicationDescriptionPlaceholder)}
        extra={<FormattedMessage {...CreatePublicationMessages.PublicationDescriptionAdditionalInfoLabel}/>}
      />
    </React.Fragment>
  );
};

export const PublicationNameAndDescriptionFormFields = compose(
  injectIntl
)(PublicationNameAndDescriptionFormFieldsComponent) as React.ComponentClass<ParentProps>;
