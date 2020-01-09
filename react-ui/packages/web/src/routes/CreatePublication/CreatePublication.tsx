import * as React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { ViewWrapper } from '../../shared/components/ViewWrapper';
import { CreatePublicationMessages } from '../../shared/i18n/CreatePublicationMessages';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { Link } from '../../shared/components/Link';
import { PageHeader } from '../../shared/components/PageHeader';
import styled from 'styled-components';
import { Col, Row } from 'antd';
import { FormField } from '../../shared/components/FormField';
import { Form, FormikProps, withFormik } from 'formik';
import {
  applyExceptionToState,
  createPublicationFormUtil,
  CreatePublicationFormValues,
  initialFormState,
  MethodError,
  withCreatePublication,
  WithCreatePublicationProps,
  withState,
  WithStateProps
} from '@narrative/shared';
import { compose } from 'recompose';
import { FormMethodError } from '../../shared/components/FormMethodError';
import { Button } from '../../shared/components/Button';
import { mediaQuery } from '../../shared/styled/utils/mediaQuery';
import PublicationPlanTable from '../../shared/components/publication/PublicationPlanTable';
import { Heading } from '../../shared/components/Heading';
import { FlexContainer } from '../../shared/styled/shared/containers';
import { openNotification } from '../../shared/utils/notificationsUtil';
import { getChannelUrl } from '../../shared/utils/channelUtils';
import { FileUpload, setFileOnFieldValueHelper } from '../../shared/components/upload/FileUpload';
import { FileUsageType } from '../../shared/utils/fileUploadUtils';
import { SEO } from '../../shared/components/SEO';
import { PublicationNameAndDescriptionFormFields } from './components/PublicationNameAndDescriptionFormFields';
import { PublicationDiscountBubble } from './components/PublicationDiscountBubble';
import { withPermissionsCardInterceptor } from '../../shared/containers/withPermissionsCardInterceptor';
import { RevokeReasonMessages } from '../../shared/i18n/RevokeReasonMessages';

const { Checkbox } = FormField;

const OrderWrapper = styled(FlexContainer)`
  
  padding: 10px;
  border: 1px solid #dfdfdf;
  margin-top: 20px;
  margin-bottom: 20px;

`;

const LeftColumnWrapper = styled.div`
  margin-bottom: 20px;
  margin-right:10px;
  padding-right: 5px;
    
  ${mediaQuery.xs`
  padding-right: 0px;
  margin-left: 10px;
  `};
  
`;

const RightColumnWrapper = styled.div`
  
  padding-left: 5px;
  margin-left: 10px;
  margin-bottom: 20px;
  
  ${mediaQuery.xs`
    padding-left: 0px;
    margin-right: 10px;
  `};
`;

export const FormGroupWrapper = styled.div`
  margin-bottom: 50px;
`;

type Props =
  RouteComponentProps<{}> &
  FormikProps<CreatePublicationFormValues> &
  WithStateProps<MethodError> &
  InjectedIntlProps &
  WithCreatePublicationProps;

const CreatePublicationComponent: React.SFC<Props> = (props) => {
  const { isSubmitting, state, setFieldValue } = props;

  return (
    <ViewWrapper style={{maxWidth: 970}}>
      <SEO title={CreatePublicationMessages.Title}/>
      <PageHeader
        style={{marginBottom: 0}}
        title={<FormattedMessage {...CreatePublicationMessages.PageTitle}/>}
        description={
          <FormattedMessage
            {...CreatePublicationMessages.PageDescription}
            values={{Publications: <Link.About type="publications" />}}
          />
        }
      />
      <OrderWrapper borderRadius="large">
        <Row>
          <Col md={10} xs={24}>
            <LeftColumnWrapper>
              <PublicationPlanTable/>
              <PublicationDiscountBubble/>
            </LeftColumnWrapper>
          </Col>
          <Col md={14} xs={24}>
            <RightColumnWrapper>
              <PageHeader
                style={{marginBottom: 10}}
                size={'small'}
                center={'title'}
                title={<FormattedMessage {...CreatePublicationMessages.FreeTrialHeader}/>}
                description={
                  <FormattedMessage
                    {...CreatePublicationMessages.FreeTrialDescription}
                  />
                }
              />
              <Form>
                <FormMethodError methodError={state.methodError} />

                <PublicationNameAndDescriptionFormFields />

                <FileUpload
                  title={CreatePublicationMessages.PublicationLogoLabel}
                  description={CreatePublicationMessages.PublicationLogoDescription}
                  onChange={setFileOnFieldValueHelper(setFieldValue, 'logo')}
                  fileUsageType={FileUsageType.PUBLICATION_LOGO}
                />
                <Heading size={4} style={{marginBottom: 10}}>
                  <FormattedMessage {...CreatePublicationMessages.PlanTypeAupLabel}/>
                </Heading>
                <Checkbox name="agreedToAup" hasFeedback={false}>
                  <FormattedMessage
                    {...CreatePublicationMessages.PlanTypeAupTermsLabel}
                    values={{AUP: <Link.Legal type="aupAcronym"/>}}
                  />
                </Checkbox>
                <FormGroupWrapper>
                  <FlexContainer alignItems="center" justifyContent="center">
                    <Button type="primary" htmlType="submit" size="large" loading={isSubmitting}>
                      <FormattedMessage {...CreatePublicationMessages.PlanLaunchPublicationButton}/>
                    </Button>
                  </FlexContainer>
                </FormGroupWrapper>
              </Form>
            </RightColumnWrapper>
          </Col>
        </Row>
      </OrderWrapper>
    </ViewWrapper>
  );
};

export default compose(
  withPermissionsCardInterceptor('createPublications', RevokeReasonMessages.CreatePublication),
  withCreatePublication,
  withState<MethodError>(initialFormState),
  injectIntl,
  withFormik<Props, CreatePublicationFormValues>({
    ...createPublicationFormUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { setState, isSubmitting, intl: { formatMessage } } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        const publication = await props.createPublication({
          ...values
        });

        openNotification.updateSuccess(
          {
            description: '',
            message: formatMessage(CreatePublicationMessages.PublicationSuccess),
            duration: 5
          });
        const publicationUrl = getChannelUrl(publication);
        props.history.push(publicationUrl);

      } catch (err) {
        applyExceptionToState(err, setErrors, setState);
      }

      setSubmitting(false);
    }
  })
)(CreatePublicationComponent) as React.ComponentClass<Props>;
