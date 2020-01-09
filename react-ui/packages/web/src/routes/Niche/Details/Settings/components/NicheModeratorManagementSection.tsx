import * as React from 'react';
import { FlexContainer } from '../../../../../shared/styled/shared/containers';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { NicheSettingsMessages } from '../../../../../shared/i18n/NicheSettingsMessages';
import { FieldProps, Form, FormikProps, withFormik } from 'formik';
import { FormControl } from '../../../../../shared/components/FormControl';
import { Select } from 'antd';
import { SelectValue } from 'antd/lib/select';
import { Button } from '../../../../../shared/components/Button';
import { SharedComponentMessages } from '../../../../../shared/i18n/SharedComponentMessages';
import {
  applyExceptionToState,
  initialFormState,
  MethodError,
  ModCenterNicheSlotSettingsFormValues,
  modCenterNicheSlotSettingsUtil,
  UpdateNicheModeratorSlotsInput,
  WithNicheModeratorSlotsProps,
  withState,
  WithStateProps,
  withUpdateNicheModeratorSlots,
  WithUpdateNicheModeratorSlotsProps,
  withNicheModeratorSlots
} from '@narrative/shared';
import { branch, compose, renderComponent, withProps } from 'recompose';
import { openNotification } from '../../../../../shared/utils/notificationsUtil';
import { FormMethodError } from '../../../../../shared/components/FormMethodError';
import styled from '../../../../../shared/styled';
import { Link } from '../../../../../shared/components/Link';
import { generatePath } from 'react-router';
import { WebRoute } from '../../../../../shared/constants/routes';
import { themeColors } from '../../../../../shared/styled/theme';
import { WithNicheDetailsContextProps } from '../../components/NicheDetailsContext';
import { ContainedLoading } from '../../../../../shared/components/Loading';
import { ChannelDetailsSection } from '../../../../../shared/components/channel/ChannelDetailsSection';

const GreenDot = styled.div`
  height: 6px;
  width: 6px;
  background: ${props => props.theme.primaryGreen};
  border-radius: 50%;
  margin-right: 5px;
`;

const Option = Select.Option;

type Props =
  WithNicheDetailsContextProps &
  InjectedIntlProps &
  FieldProps<{}> &
  WithNicheModeratorSlotsProps &
  FormikProps<ModCenterNicheSlotSettingsFormValues> &
  WithStateProps<MethodError>;

const NicheModeratorManagementSectionComponent: React.SFC<Props> = (props) => {
  const {
    activeModeratorElection,
    electionIsLive,
    setFieldValue,
    values,
    isSubmitting,
    state
  } = props;

  const extra = electionIsLive && (
    <FlexContainer alignItems="center">
      <GreenDot/>

      <Link.Anchor
        target="_self"
        href={generatePath(WebRoute.ModeratorElectionDetails, {electionOid: activeModeratorElection.oid})}
        style={{color: themeColors.primaryGreen}}
      >
        <FormattedMessage {...NicheSettingsMessages.ModeratorNominationsOpen}/>
      </Link.Anchor>
    </FlexContainer>
  );

  return (
    <ChannelDetailsSection
      title={<FormattedMessage {...NicheSettingsMessages.SectionModeratorManagement}/>}
      extra={extra}
    >
      <Form>
        <FormMethodError methodError={state.methodError}/>

        <FlexContainer>
          <FormControl
            style={{ marginBottom: 0 }}
            label={<FormattedMessage {...NicheSettingsMessages.ModeratorCountLabel}/>}
          >
            <Select
              /* TODO: Uncomment after beta
              disabled={electionIsLive} */
              size="large"
              defaultValue="1"
              style={{ width: 250, marginTop: 10 }}
              value={values.moderatorSlots}
              onChange={(value: SelectValue) => setFieldValue('moderatorSlots', value)}
            >
              {Array(30).fill(0).map((_, index) =>
                <Option key={index.toString()} value={index + 1}>{index + 1}</Option>
              )}
            </Select>
          </FormControl>

          <Button
            size="large"
            type="primary"
            htmlType="submit"
            // TODO: Uncomment after beta
            // disabled={electionIsLive}
            loading={isSubmitting}
            style={{ minWidth: 120, alignSelf: 'flex-end', marginLeft: 25 }}
          >
            <FormattedMessage {...SharedComponentMessages.SaveBtnText}/>
          </Button>
        </FlexContainer>

        {/* TODO: Uncomment after beta*/}
        {/*{electionIsLive &&*/}
        {/*<span style={{color: themeColors.primaryRed}}>*/}
        {/*<FormattedMessage {...NicheSettingsMessages.CannotChangeDuringElectionMsg}/>*/}
        {/*</span>*/}
        {/*}*/}
      </Form>
    </ChannelDetailsSection>
  );
};

export const NicheModeratorManagementSection = compose(
  // jw: first thing we need to do is load the moderator slot details!
  withProps((props: WithNicheDetailsContextProps) => {
    const { nicheDetail: { niche } } = props;
    return { nicheId: niche.oid};
  }),
  withNicheModeratorSlots,
  // jw: because the formik setup requires the results from the above HOC, we need to prevent this stack from descending
  //     in a loading state. If loading moves through then formik is gonna get messed up and our form won't have proper
  //     defaults.
  branch((props: WithNicheModeratorSlotsProps) => props.nicheModeratorSlotsLoading,
    renderComponent(() => (
      <ChannelDetailsSection title={<FormattedMessage {...NicheSettingsMessages.SectionModeratorManagement}/>}>
        <ContainedLoading />
      </ChannelDetailsSection>
    ))
  ),

  withState<MethodError>(initialFormState),
  withUpdateNicheModeratorSlots,
  injectIntl,
  withFormik<Props & WithUpdateNicheModeratorSlotsProps, ModCenterNicheSlotSettingsFormValues>({
    ...modCenterNicheSlotSettingsUtil,
    mapPropsToValues: (props: Props) => {
      const { moderatorSlots } = props;
      return modCenterNicheSlotSettingsUtil.mapPropsToValues({ moderatorSlots });
    },
    handleSubmit: async (values, { props, setErrors, setSubmitting }) => {
      const {
        setState,
        updateNicheModeratorSlots,
        isSubmitting,
        nicheDetail: { niche }
      } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null, isSubmitting: true}));

      try {
        const input: UpdateNicheModeratorSlotsInput = { ...values };

        await updateNicheModeratorSlots({input, nicheOid: niche.oid});

        openNotification.updateSuccess(
          {
            description: '',
            message: props.intl.formatMessage(NicheSettingsMessages.ModeratorSlotsUpdateSuccessful),
            duration: 5
          });
      } catch (e) {
        applyExceptionToState(e, setErrors, setState);
      }

      setSubmitting(false);
    }
  })
)(NicheModeratorManagementSectionComponent) as React.ComponentClass<WithNicheDetailsContextProps>;
