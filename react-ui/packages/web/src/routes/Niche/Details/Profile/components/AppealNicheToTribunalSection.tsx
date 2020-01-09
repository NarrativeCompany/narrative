import * as React from 'react';
import { generatePath, RouteComponentProps, withRouter } from 'react-router';
import { compose, withHandlers, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { Link, LinkSecurerFunction } from '../../../../../shared/components/Link';
import { AppealNicheToTribunalModal, AppealNicheToTribunalModalProps } from './AppealNicheToTribunalModal';
import { PermissionErrorModal } from '../../../../../shared/components/PermissionErrorModal';
import { NicheProfileStatusMessages } from '../../../../../shared/i18n/NicheProfileStatusMessages';
import { RevokeReasonMessages } from '../../../../../shared/i18n/RevokeReasonMessages';
import { WebRoute } from '../../../../../shared/constants/routes';
import { canCurrentUserSubmitTribunalAppeal } from '../../../../../shared/utils/tribunalIssuesUtil';
import { withState, WithStateProps } from '@narrative/shared';
import {
  withPermissionsModalHelpers,
  WithPermissionsModalHelpersProps
} from '../../../../../shared/containers/withPermissionsModalHelpers';
import { WithNicheDetailsContextProps } from '../../components/NicheDetailsContext';
import { WithLoginModalHelpersProps } from '../../../../../shared/containers/withLoginModalHelpers';
import { EnhancedNicheStatus } from '../../../../../shared/enhancedEnums/nicheStatus';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { ChannelDetailsSection } from '../../../../../shared/components/channel/ChannelDetailsSection';

interface State {
  isAppealingNiche?: boolean;
}

interface WithShowNicheAppealModalProps {
  showNicheAppealModal: () => void;
}

interface WithHandlers extends WithShowNicheAppealModalProps {
  handleNicheAppeal: (tribunalIssueOid?: string) => void;
}

interface WithProps {
  linkSecurer?: LinkSecurerFunction;
  appealModalProps?: AppealNicheToTribunalModalProps;
}

type Props =
  WithProps &
  WithShowNicheAppealModalProps &
  WithPermissionsModalHelpersProps &
  WithNicheDetailsContextProps;

const AppealNicheToTribunalSectionComponent: React.SFC<Props> = (props) => {
  const { linkSecurer, appealModalProps, permissionErrorModalProps, showNicheAppealModal, nicheDetail } = props;

  const nicheStatus = EnhancedNicheStatus.get(nicheDetail.niche.status);

  const showAppeal = !!linkSecurer || !!appealModalProps;

  const acceptableUsePolicyLink = <Link.Legal type="aup"/>;
  const termsOfServiceLink = <Link.Legal type="tos"/>;

  return (
    <ChannelDetailsSection title={<FormattedMessage {...NicheProfileStatusMessages.NicheStatusSectionTitle}/>}>
      <Paragraph size="large" marginBottom="large" style={{fontWeight: 'bold'}}>
        <FormattedMessage {...nicheStatus.message}/>
      </Paragraph>

      {showAppeal &&
        <React.Fragment>
          <Paragraph marginBottom="large">
            <FormattedMessage {...nicheStatus.isRejected() ?
              NicheProfileStatusMessages.AppealRejectedNicheQuestion :
              NicheProfileStatusMessages.AppealActiveNicheQuestion}
              values={{termsOfServiceLink, acceptableUsePolicyLink}}
            />
          </Paragraph>
          <Paragraph marginBottom="large">
            <FormattedMessage {...nicheStatus.isRejected() ?
              NicheProfileStatusMessages.AppealRejectedNicheNote :
              NicheProfileStatusMessages.AppealActiveNicheNote}
            />
          </Paragraph>
          <Paragraph marginBottom="large">
            <Link.Anchor onClick={showNicheAppealModal} linkSecurer={linkSecurer}>
              <FormattedMessage {...nicheStatus.isRejected() ?
                NicheProfileStatusMessages.AppealToTribunalToApprove :
                NicheProfileStatusMessages.AppealToTribunalToReject}/>
            </Link.Anchor>
          </Paragraph>
        </React.Fragment>
      }

      {appealModalProps && <AppealNicheToTribunalModal {...appealModalProps}/>}
      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps}/>}
    </ChannelDetailsSection>
  );
};

type HandlerProps =
  WithStateProps<State> &
  RouteComponentProps;

export const AppealNicheToTribunalSection = compose(
  withRouter,
  withPermissionsModalHelpers(
    'submitTribunalAppeals',
    RevokeReasonMessages.SubmitTribunalAppeal,
    RevokeReasonMessages.SubmitTribunalAppealTimeout
  ),
  withState<State>({}),
  withHandlers({
    showNicheAppealModal: (props: HandlerProps) => () => {
      props.setState(ss => ({...ss, isAppealingNiche: true}));
    },
    handleNicheAppeal: (props: HandlerProps) => (tribunalIssueOid: string) => {
      const { history, setState } = props;

      setState(ss => ({...ss, isAppealingNiche: undefined}));

      if (tribunalIssueOid) {
        history.push(generatePath(WebRoute.AppealDetails , {tribunalIssueOid}));
      }
    }
  }),
  withProps((
    props: WithHandlers & Props & WithNicheDetailsContextProps & WithLoginModalHelpersProps & WithStateProps<State>
  ) => {
    const { nicheDetail, loginLinkSecurer, permissionLinkSecurer } = props;

    let linkSecurer: LinkSecurerFunction | undefined;
    let appealModalProps: AppealNicheToTribunalModalProps | undefined;

    // jw: the only time we will have a loginLinkSecurer is for a guest, so that makes this easy.
    // jw: note: we need to do this because guests will not pass the canCurrentUserSubmitTribunalAppeal, but we still
    //     want to show the link and prompt them to sign in.
    if (loginLinkSecurer) {
      linkSecurer = loginLinkSecurer;

    // jw: The permissionLinkSecurer will prevent the modal from loading, providing the permission modal instead
    } else if (canCurrentUserSubmitTribunalAppeal(nicheDetail.availableTribunalIssueTypes)) {
      linkSecurer = permissionLinkSecurer;

      const { state, setState, handleNicheAppeal } = props;
      appealModalProps = {
        nicheDetail,
        visible: state.isAppealingNiche,
        // jw: because our handler takes a parameter we can't pass it here directly because the event object from
        //     click events will be detected as the tribunalIssueOid. Instead, we need to wrap it and invoke with no
        //     parameter. I'm disappointed that the TypeScript compiler allows us to use a method with a different
        //     signature here.
        dismiss: () => setState(ss => ({...ss, isAppealingNiche: undefined})),
        onSubmitSuccess: handleNicheAppeal
      };
    }

    return { linkSecurer, appealModalProps };
  }),
)(AppealNicheToTribunalSectionComponent) as React.ComponentClass<WithNicheDetailsContextProps>;
