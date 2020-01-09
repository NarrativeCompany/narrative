import { DialogHeader } from '../DialogHeader';
import { FormattedMessage } from 'react-intl';
import { DeleteAccountMessages } from '../../../../shared/i18n/DeleteAccountMessages';
import * as React from 'react';
import { Icon } from 'antd';
import { AuthHeader } from '../../../../shared/styled/shared/auth';
import { branch, compose, renderComponent, withHandlers, withProps } from 'recompose';
import {
  withCurrentUserOwnedChannels,
  WithCurrentUserOwnedChannelsProps,
  withState,
  WithStateProps,
  MethodError
} from '@narrative/shared';
import { CheckboxChangeEvent } from 'antd/lib/checkbox';
import { Checkbox } from '../../../../shared/components/Checkbox';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { Heading } from '../../../../shared/components/Heading';
import { ContinueCancelButtons } from '../ContinueCancelButtons';
import { ContainedLoading } from '../../../../shared/components/Loading';
import { Link } from '../../../../shared/components/Link';
import { SharedComponentMessages } from '../../../../shared/i18n/SharedComponentMessages';
import { FormControl } from '../../../../shared/components/FormControl';

interface ParentProps extends MethodError {
  // tslint:disable no-any
  handleContinue: () => any;
  handleDismiss: () => any;
  // tslint:enable no-any
}

interface State {
  ackProfileWillBeRemoved?: boolean;
  ackContentWillBeRemoved?: boolean;
  ackNRVEBalanceWillBeLost?: boolean;
  ackNichesWillBeLost?: boolean;
  ackPublicationsWillBeLost?: boolean;
  ackActionCannotBeUndone?: boolean;
}

interface WithProps {
  ownedNiches: number;
  ownedPublications: number;
}

interface WithHandlers {
  handleCheckboxChange: (e: CheckboxChangeEvent) => void;
}

type Props =
  ParentProps &
  WithProps &
  WithStateProps<State> &
  WithHandlers;

const DeleteAccountAckCBPanelComponent: React.SFC<Props> = (props) => {
  const {
    state,
    ownedNiches,
    ownedPublications,
    methodError,
    handleContinue,
    handleDismiss,
    handleCheckboxChange
  } = props;

  const buttonEnabled = () => {
    return state.ackActionCannotBeUndone &&
      state.ackNRVEBalanceWillBeLost &&
      state.ackProfileWillBeRemoved &&
      state.ackContentWillBeRemoved &&
      (ownedNiches <= 0 || state.ackNichesWillBeLost) &&
      (ownedPublications <= 0 || state.ackPublicationsWillBeLost);
  };

  const nrveLink = <Link.About type="nrve"/>;

  return (
    <React.Fragment>

        <DialogHeader
          icon={<Icon type="exclamation-circle" style={{fontSize: 32, color: 'RED'}}/>}
          title={
            <AuthHeader style={{color: 'RED', fontWeight: 'lighter'}}>
              <FormattedMessage {...DeleteAccountMessages.PageTitle}/>
            </AuthHeader>
          }
          description={
            <React.Fragment>
              <FormattedMessage {...DeleteAccountMessages.SummaryMessage}/>
              <FormattedMessage {...DeleteAccountMessages.ActionCannotBeUndone}/>
            </React.Fragment>
          }
          includeFormMethodError={true}
          methodError={methodError}
          showDivider={true}
        />

      <FlexContainer column={true}  justifyContent="flex-start">

        <Heading size={5} font-weight={900} style={{marginBottom: 20}}>
           <FormattedMessage {...DeleteAccountMessages.IUnderstandLabel}/>
        </Heading>

        <FormControl>
          <Checkbox name="ackProfileWillBeRemoved" onChange={handleCheckboxChange}>
            <FormattedMessage {...DeleteAccountMessages.ProfileWillBeRemovedLabel}/>
          </Checkbox>
        </FormControl>

        <FormControl>
          <Checkbox name="ackContentWillBeRemoved" onChange={handleCheckboxChange}>
            <FormattedMessage {...DeleteAccountMessages.ContentWillBeRemovedLabel}/>
          </Checkbox>
        </FormControl>

        <FormControl>
          <Checkbox name="ackNRVEBalanceWillBeLost" onChange={handleCheckboxChange}>
            <FormattedMessage {...DeleteAccountMessages.NRVEBalanceLostLabel} values={{nrveLink}}/>
          </Checkbox>
        </FormControl>

        {ownedNiches > 0 &&
          <FormControl>
            <Checkbox name="ackNichesWillBeLost" onChange={handleCheckboxChange}>
              <FormattedMessage {...DeleteAccountMessages.NichesLostLabel} values={{ownedNiches}}/>
            </Checkbox>
          </FormControl>
        }

        {ownedPublications > 0 &&
          <FormControl
            extra={<FormattedMessage {...DeleteAccountMessages.PublicationsLostDescriptionDeleteAccount}/>}
          >
            <Checkbox name="ackPublicationsWillBeLost" onChange={handleCheckboxChange}>
              <FormattedMessage {...DeleteAccountMessages.PublicationsLostLabel} values={{ownedPublications}}/>
            </Checkbox>
          </FormControl>
        }

        <FormControl>
          <Checkbox name="ackActionCannotBeUndone" onChange={handleCheckboxChange}>
            <FormattedMessage {...DeleteAccountMessages.ActionCannotBeUndone}/>
          </Checkbox>
        </FormControl>

      </FlexContainer>

      <ContinueCancelButtons
        continueLabel={<FormattedMessage {...DeleteAccountMessages.Continue}/>}
        cancelLabel={<FormattedMessage {...SharedComponentMessages.Cancel}/>}
        handleContinue={handleContinue}
        continueEnabled={buttonEnabled()}
        handleCancel={handleDismiss}
        stackVertical={true}
        continueStyle={{width: 200}}
      />

    </React.Fragment>
  );
};

export const DeleteAccountAckCBPanel = compose(
  withCurrentUserOwnedChannels,
  withState({}),
  branch((props: WithCurrentUserOwnedChannelsProps) => props.currentUserOwnedChannelsData.loading,
    renderComponent(() => <ContainedLoading/>)
  ),
  withProps((props: WithCurrentUserOwnedChannelsProps) => {
    const { currentUserOwnedChannelsData: {getCurrentUserOwnedChannels} } = props;
    const ownedNiches =
      getCurrentUserOwnedChannels &&
      getCurrentUserOwnedChannels.ownedNiches;
    const ownedPublications =
      getCurrentUserOwnedChannels &&
      getCurrentUserOwnedChannels.ownedPublications;

    return { ownedNiches, ownedPublications };
  }),
  withHandlers({
    handleCheckboxChange: (props: WithStateProps<State>) => (e: CheckboxChangeEvent) => {
      props.setState(ss => ({
        ...ss,
        [e.target.name as string]: e.target.checked
      }));
    }
  })
)(DeleteAccountAckCBPanelComponent) as React.ComponentClass<ParentProps>;
