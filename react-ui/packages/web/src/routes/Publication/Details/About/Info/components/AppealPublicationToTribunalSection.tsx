import * as React from 'react';
import { generatePath, RouteComponentProps, withRouter } from 'react-router';
import { compose, withHandlers, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { Link, LinkSecurerFunction } from '../../../../../../shared/components/Link';
import {
  AppealPublicationToTribunalModal,
  AppealPublicationToTribunalModalProps
} from './AppealPublicationToTribunalModal';
import { PermissionErrorModal } from '../../../../../../shared/components/PermissionErrorModal';
import { RevokeReasonMessages } from '../../../../../../shared/i18n/RevokeReasonMessages';
import { WebRoute } from '../../../../../../shared/constants/routes';
import { PublicationProfile, withState, WithStateProps } from '@narrative/shared';
import {
  withPermissionsModalHelpers,
  WithPermissionsModalHelpersProps
} from '../../../../../../shared/containers/withPermissionsModalHelpers';
import {
  PublicationDetailsConnect,
  WithPublicationDetailsContextProps
} from '../../../../components/PublicationDetailsContext';
import { WithLoginModalHelpersProps } from '../../../../../../shared/containers/withLoginModalHelpers';
import { Paragraph } from '../../../../../../shared/components/Paragraph';
import { ChannelDetailsSection } from '../../../../../../shared/components/channel/ChannelDetailsSection';
import { PublicationAboutStatusMessages } from '../../../../../../shared/i18n/PublicationAboutStatusMessages';
import { LocalizedTime } from '../../../../../../shared/components/LocalizedTime';

interface State {
  isAppealingPublication?: boolean;
}

interface WithShowPublicationAppealModalProps {
  showPublicationAppealModal: () => void;
}

interface WithHandlers extends WithShowPublicationAppealModalProps {
  handlePublicationAppeal: (tribunalIssueOid?: string) => void;
}

interface WithProps {
  linkSecurer?: LinkSecurerFunction;
  appealModalProps?: AppealPublicationToTribunalModalProps;
}

interface ParentProps {
  publicationProfile: PublicationProfile;
}

type Props =
  WithProps &
  WithShowPublicationAppealModalProps &
  WithPermissionsModalHelpersProps &
  WithPublicationDetailsContextProps &
  ParentProps;

const AppealPublicationToTribunalSectionComponent: React.SFC<Props> = (props) => {
  const {
    linkSecurer,
    appealModalProps,
    permissionErrorModalProps,
    showPublicationAppealModal,
    publicationProfile
  } = props;

  const showAppeal = !!linkSecurer || !!appealModalProps;

  const creationDatetime = <LocalizedTime time={publicationProfile.creationDatetime} dateOnly={true}/>;
  const followerCount = publicationProfile.followerCount;

  const acceptableUsePolicyLink = <Link.Legal type="aup"/>;
  const termsOfServiceLink = <Link.Legal type="tos"/>;

  return (
    <ChannelDetailsSection title={<FormattedMessage {...PublicationAboutStatusMessages.StatusSectionTitle}/>}>
      <Paragraph marginBottom="large">
        <FormattedMessage
          {...PublicationAboutStatusMessages.StatusIntro}
          values={{creationDatetime, followerCount}}
        />
      </Paragraph>

      {showAppeal &&
        <React.Fragment>
          <Paragraph marginBottom="large">
            <FormattedMessage
              {...PublicationAboutStatusMessages.AppealPublicationNote}
              values={{termsOfServiceLink, acceptableUsePolicyLink}}
            />
          </Paragraph>
          <Paragraph marginBottom="large">
            <Link.Anchor onClick={showPublicationAppealModal} linkSecurer={linkSecurer}>
              <FormattedMessage {...PublicationAboutStatusMessages.AppealToTribunalToReject}/>
            </Link.Anchor>
          </Paragraph>
        </React.Fragment>
      }

      {appealModalProps && <AppealPublicationToTribunalModal {...appealModalProps}/>}
      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps}/>}
    </ChannelDetailsSection>
  );
};

type HandlerProps =
  WithStateProps<State> &
  RouteComponentProps;

export const AppealPublicationToTribunalSection = compose(
  withRouter,
  withPermissionsModalHelpers(
    'submitTribunalAppeals',
    RevokeReasonMessages.SubmitTribunalAppeal,
    RevokeReasonMessages.SubmitTribunalAppealTimeout
  ),
  withState<State>({}),
  withHandlers({
    showPublicationAppealModal: (props: HandlerProps) => () => {
      props.setState(ss => ({...ss, isAppealingPublication: true}));
    },
    handlePublicationAppeal: (props: HandlerProps) => (tribunalIssueOid: string) => {
      const { history, setState } = props;

      setState(ss => ({...ss, isAppealingPublication: undefined}));

      if (tribunalIssueOid) {
        history.push(generatePath(WebRoute.AppealDetails , {tribunalIssueOid}));
      }
    }
  }),
  PublicationDetailsConnect,
  withProps((
    props: WithHandlers & Props & WithLoginModalHelpersProps & WithStateProps<State>
  ) => {
    const { publicationDetail, publicationProfile, loginLinkSecurer, permissionLinkSecurer } = props;

    let linkSecurer: LinkSecurerFunction | undefined;
    let appealModalProps: AppealPublicationToTribunalModalProps | undefined;

    // jw: the only time we will have a loginLinkSecurer is for a guest, so that makes this easy.
    // jw: note: we need to do this because guests will not pass the canCurrentUserSubmitTribunalAppeal, but we still
    //     want to show the link and prompt them to sign in.
    if (loginLinkSecurer) {
      linkSecurer = loginLinkSecurer;

    // jw: The permissionLinkSecurer will prevent the modal from loading, providing the permission modal instead
    } else if (publicationProfile.canCurrentUserAppeal) {
      linkSecurer = permissionLinkSecurer;

      const { state, setState, handlePublicationAppeal } = props;
      appealModalProps = {
        publicationDetail,
        visible: state.isAppealingPublication,
        // jw: because our handler takes a parameter we can't pass it here directly because the event object from
        //     click events will be detected as the tribunalIssueOid. Instead, we need to wrap it and invoke with no
        //     parameter. I'm disappointed that the TypeScript compiler allows us to use a method with a different
        //     signature here.
        dismiss: () => setState(ss => ({...ss, isAppealingPublication: undefined})),
        onSubmitSuccess: handlePublicationAppeal
      };
    }

    return { linkSecurer, appealModalProps };
  }),
)(AppealPublicationToTribunalSectionComponent) as React.ComponentClass<ParentProps>;
