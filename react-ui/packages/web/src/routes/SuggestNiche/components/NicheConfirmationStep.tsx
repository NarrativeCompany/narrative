import * as React from 'react';
import { compose } from 'recompose';
import { Col, Icon, Row } from 'antd';
import { withFormik, Form, FormikProps } from 'formik';
import { StepWrapper } from './FormStepWrapper';
import { NicheCard } from '../../HQ/components/NicheCard';
import { NicheCardProgressBar } from '../../HQ/components/NicheCardProgressBar';
import { NicheCardUser } from '../../HQ/components/NicheCardUser';
import { ChannelCardTitleAndDesc } from '../../HQ/components/ChannelCardTitleAndDesc';
import { CheckboxField } from '../../../shared/components/CheckboxField';
import { Heading } from '../../../shared/components/Heading';
import { Link } from '../../../shared/components/Link';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { FormattedMessage } from 'react-intl';
import { NicheConfirmationStepMessages } from '../../../shared/i18n/NicheConfirmationStepMessages';
import { themeColors } from '../../../shared/styled/theme';
import styled from '../../../shared/styled/index';
import {
  User,
  Referendum,
  ReferendumVoteReason,
  applyExceptionToState,
  CreateNicheInput,
  withCreateNiche,
  WithCreateNicheProps,
  CreateNicheFormValues,
  createNicheFormUtil,
  withState,
  WithStateProps,
  MethodError,
  initialFormState
} from '@narrative/shared';
import { FormMethodError } from '../../../shared/components/FormMethodError';
import { FormButtonGroup } from '../../../shared/components/FormButtonGroup';
import { EnhancedReferendumVoteReason } from '../../../shared/enhancedEnums/referendumVoteReason';

const EditDetails = styled<FlexContainerProps>(FlexContainer)`
  margin: 15px 0 10px;
  
  a {
    color: ${props => props.theme.secondaryBlue};
  }
`;

const VerifySection = styled.div`
  margin: 20px 0 30px;
  border-bottom: 1px solid #e9e9e9;
`;

const NicheAssertionList = styled.ul`
  margin-bottom: 0;
  
  li {
    line-height: 1.5;
  }
`;

// tslint:disable no-any
interface ParentProps {
  onEditDetailsClick: () => any;
  onPrevClick: () => any;
  onNicheCreated: (referendum: Referendum) => void;
  name: string;
  description: string;
  currentUser?: User;
}
// tslint:enable no-any

type Props =
  ParentProps &
  WithStateProps<MethodError> &
  FormikProps<CreateNicheFormValues> &
  WithCreateNicheProps;

export const NicheConfirmationStepComponent: React.SFC<Props> = (props) => {
  const { name, description, currentUser, isValid, state, onPrevClick, isSubmitting } = props;

  if (!currentUser) {
    // jw:todo:error-handling: We need to report the server that a guest made it this far into th suggestion process.
    return null;
  }

  const progressBar =
    <NicheCardProgressBar percent={!isValid ? 0 : 100} strokeColor={themeColors.secondaryBlue}/>;
  const termsOfService = <Link.Legal type="tos"/>;
  const acceptableUsePolicy = <Link.Legal type="aup"/>;

  const BackBtn = (
    <React.Fragment>
      <Icon type="left"/> <FormattedMessage {...NicheConfirmationStepMessages.BackBtnText}/>
    </React.Fragment>
  );

  return (
    <StepWrapper
      title={<FormattedMessage {...NicheConfirmationStepMessages.PageHeaderTitle}/>}
      description={<FormattedMessage {...NicheConfirmationStepMessages.PageHeaderDescription}/>}
    >
      <Row gutter={16}>
        <Col
          sm={{span: 24}} md={{span: 20, offset: 2}} lg={{span: 18, offset: 3}}
          style={{marginBottom: 30, marginTop: 30}}
        >
          <NicheCard cover={progressBar}>
            <NicheCardUser user={currentUser} targetBlank={true} />

            <ChannelCardTitleAndDesc
              title={name || ''}
              description={description || ''}
              center={true}
            />

            <EditDetails>
              <Link.Anchor onClick={props.onEditDetailsClick}>
                <FormattedMessage {...NicheConfirmationStepMessages.EditBtnText}/>
              </Link.Anchor>
            </EditDetails>
          </NicheCard>
        </Col>
      </Row>

      <Form>
        <Row gutter={16}>
          <Col span={24}>
            <VerifySection>
              <FormMethodError methodError={state.methodError}/>

              <Heading size={4}>
                <FormattedMessage {...NicheConfirmationStepMessages.FormItemLabel}/>
              </Heading>

              <CheckboxField
                name="assertChecked"
                hasFeedback={false}
                style={{marginBottom: 0}}
              >
                <FormattedMessage {...NicheConfirmationStepMessages.CheckboxTextOne}/>
              </CheckboxField>

              <NicheAssertionList>
                {EnhancedReferendumVoteReason.enhancers
                  .filter((enhancedReason) => (enhancedReason.reason !== ReferendumVoteReason.VIOLATES_TOS))
                  .map((enhancedReason) => (
                    <li key={enhancedReason.reason}>
                      <FormattedMessage {...enhancedReason.assertionMessage}/>
                    </li>
                ))}
              </NicheAssertionList>

              <CheckboxField
                name="agreeChecked"
                hasFeedback={false}
              >
                <FormattedMessage
                  {...EnhancedReferendumVoteReason.get(ReferendumVoteReason.VIOLATES_TOS).assertionMessage}
                  values={{termsOfService, acceptableUsePolicy}}
                />
              </CheckboxField>
            </VerifySection>
          </Col>
        </Row>

        <Row gutter={16}>
          <Col span={24}>
            <FormButtonGroup
              btnText={<FormattedMessage {...NicheConfirmationStepMessages.BtnText}/>}
              linkText={BackBtn}
              btnProps={{style: {minWidth: 180}, htmlType: 'submit', loading: isSubmitting}}
              linkProps={{onClick: onPrevClick}}
            />
          </Col>
        </Row>
      </Form>
    </StepWrapper>
  );
};

export const NicheConfirmationStep = compose(
  withState<MethodError>(initialFormState),
  withCreateNiche,
  withFormik<Props, CreateNicheFormValues>({
    ...createNicheFormUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { createNiche, onNicheCreated, setState, name, description, isSubmitting } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        const input: CreateNicheInput = {...values, name, description};
        const referendum = await createNiche(input);

        setSubmitting(false);
        onNicheCreated(referendum);
      } catch (err) {
        applyExceptionToState(err, setErrors, setState);
        setSubmitting(false);
      }
    }
  })
)(NicheConfirmationStepComponent) as React.ComponentClass<ParentProps>;
