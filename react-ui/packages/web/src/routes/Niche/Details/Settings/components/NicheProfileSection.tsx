import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import { withFormik, FormikProps, Form } from 'formik';
import { FormField } from '../../../../../shared/components/FormField';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { Link } from '../../../../../shared/components/Link';
import { FormMethodError } from '../../../../../shared/components/FormMethodError';
import { FormattedMessage, injectIntl } from 'react-intl';
import { SharedComponentMessages } from '../../../../../shared/i18n/SharedComponentMessages';
import {
  NicheDetailsFormValues as EditNicheDetailsFormValues,
  editNicheDetailsFormUtil,
  withUpdateNiche,
  WithUpdateNicheProps,
  UpdateNicheInput,
  withState,
  WithStateProps,
  applyExceptionToState,
  MethodError,
  initialFormState,
  Niche,
  TribunalIssueDetail,
  withNicheProfile,
  WithNicheProfileProps,
  TribunalIssue,
  maxNicheNameLength
} from '@narrative/shared';
import { NicheSettingsMessages } from '../../../../../shared/i18n/NicheSettingsMessages';
import { generatePath, RouteComponentProps } from 'react-router';
import { ContainedLoading } from '../../../../../shared/components/Loading';
import { WebRoute } from '../../../../../shared/constants/routes';
import { Button, ButtonProps } from '../../../../../shared/components/Button';
import { textAreaNewlineRemovalProps } from '../../../../../shared/utils/removeNewlinesFromTextarea';
import { WithNicheDetailsContextProps } from '../../components/NicheDetailsContext';
import styled from '../../../../../shared/styled';
import { Card } from '../../../../../shared/components/Card';
import { NicheNameAndDescStepMessages } from '../../../../../shared/i18n/NicheNameAndDescStepMessages';
import { ChannelDetailsSection } from '../../../../../shared/components/channel/ChannelDetailsSection';

const StyledButton = styled<ButtonProps>(Button)`
  &.ant-btn {
    min-width: 200px;
    display: block;
    margin-top: 10px;
  }
`;

interface WithProps {
  nicheId: string;
  niche: Niche;
  tribunalIssue: TribunalIssue;
}

type Props =
  WithProps &
  RouteComponentProps &
  WithStateProps<MethodError> &
  FormikProps<EditNicheDetailsFormValues> &
  WithUpdateNicheProps;

const NicheProfileSectionComponent: React.SFC<Props> = (props) => {
  const { state, isSubmitting, tribunalIssue, setFieldValue, niche } = props;

  const termsOfService = <Link.Legal type="tos"/>;
  const acceptableUsePolicy = <Link.Legal type="aup"/>;

  const maxNicheNameInputLength = Math.max(maxNicheNameLength, niche ? niche.name.length : 0);

  return (
    <ChannelDetailsSection
      title={<FormattedMessage {...NicheSettingsMessages.NicheProfileTitle}/>}
      description={<FormattedMessage
        {...NicheSettingsMessages.NicheProfileDefinition}
        values={{termsOfService, acceptableUsePolicy}}
      />}
    >
      {tribunalIssue &&
        <Card
          noBoxShadow={true}
          color="lightBlue">
          <Paragraph  textAlign="center" size="large">
            <FormattedMessage {...NicheSettingsMessages.OpenTribunalIssueMessage} />
            {' '}
            <Link.Anchor
              target="_self"
              href={generatePath(WebRoute.AppealDetails, { tribunalIssueOid: tribunalIssue.oid })}
            >
              <FormattedMessage {...NicheSettingsMessages.OpenTribunalIssueLink}/>
            </Link.Anchor>
          </Paragraph>
        </Card>
      }

      <Form>
        <FormMethodError methodError={state.methodError}/>

        <FormField.Input
          name="name"
          disabled={!!tribunalIssue}
          label={<FormattedMessage {...NicheSettingsMessages.NicheNameFieldLabel}/>}
          maxLength={maxNicheNameInputLength}
        />

        <FormField.TextArea
          name="description"
          disabled={!!tribunalIssue}
          label={<FormattedMessage {...NicheSettingsMessages.NicheDescriptionFieldLabel}/>}
          rows={4}
          extra={<FormattedMessage {...NicheNameAndDescStepMessages.DefinitionFieldPlaceholder}/>}
          {...textAreaNewlineRemovalProps('description', setFieldValue)}
        />

        <Paragraph color="dark" marginBottom="large">
          <FormattedMessage {...NicheSettingsMessages.FormHelpText}/>
        </Paragraph>

        <StyledButton
          disabled={!!tribunalIssue}
          size="large"
          type="primary"
          htmlType="submit"
          loading={isSubmitting}>
          <FormattedMessage {...SharedComponentMessages.SubmitBtnText}/>
        </StyledButton>
      </Form>
    </ChannelDetailsSection>
  );
};

export const NicheProfileSection = compose(
  injectIntl,
  withState<MethodError>(initialFormState),
  withUpdateNiche,
  withProps((props: WithNicheDetailsContextProps) => {
    const { nicheDetail: { niche }  } = props;

    return { nicheId: niche.oid };
  }),
  withNicheProfile,
  // jw: we cannot let the withNicheProfile reach formik in a loading state. If it does then the form will not have
  //     proper defaults.
  branch((props: WithNicheProfileProps) => props.nicheProfileData.loading,
    renderComponent(() => (
      <ChannelDetailsSection title={<FormattedMessage {...NicheSettingsMessages.NicheProfileTitle}/>}>
        <ContainedLoading/>
      </ChannelDetailsSection>
    ))
  ),

  withProps((props: Props & WithNicheProfileProps) => {
    const { nicheProfileData } = props;

    const niche =
      nicheProfileData &&
      nicheProfileData.getNicheProfile &&
      nicheProfileData.getNicheProfile.niche;

    const tribunalIssue =
      nicheProfileData &&
      nicheProfileData.getNicheProfile &&
      nicheProfileData.getNicheProfile.editDetailsTribunalIssue;

    return { niche, tribunalIssue };
  }),
  withFormik<Props, EditNicheDetailsFormValues>({
      ...editNicheDetailsFormUtil,
    mapPropsToValues: (props: Props) => {
        const { niche } = props;

        const name =
          niche &&
          niche.name;

        const description =
          niche &&
          niche.description;

        return editNicheDetailsFormUtil.mapPropsToValues({name, description});
    },
    handleSubmit: async (values, { props, setErrors, setSubmitting }) => {
      const { setState, updateNiche, niche, isSubmitting, history } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null, isSubmitting: true}));

      try {
        const input: UpdateNicheInput = { name: values.name, description: values.description };

        const tribunalIssueDetail: TribunalIssueDetail = await updateNiche(input, niche.oid);

        const tribunalIssueOid =
          tribunalIssueDetail &&
          tribunalIssueDetail.tribunalIssue &&
          tribunalIssueDetail.tribunalIssue.oid;

        history.push(generatePath(WebRoute.AppealDetails , {tribunalIssueOid}));
      } catch (e) {
        applyExceptionToState(e, setErrors, setState);
      }

      setSubmitting(false);
    }
  })
)(NicheProfileSectionComponent) as React.ComponentClass<{}>;
