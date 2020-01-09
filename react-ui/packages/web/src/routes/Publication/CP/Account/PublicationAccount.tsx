import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import {
  withPublicationPlanDetail,
  WithPublicationPlanDetailProps,
  withState,
  WithStateProps,
  PublicationPlanType
} from '@narrative/shared';
import { fullPlaceholder, withLoadingPlaceholder } from '../../../../shared/utils/withLoadingPlaceholder';
import { PurchasePublicationPlanModal } from './components/PurchasePublicationPlanModal';
import { FormattedMessage } from 'react-intl';
import { PublicationDetailsMessages } from '../../../../shared/i18n/PublicationDetailsMessages';
import {
  WithPublicationDetailsContextProps
} from '../../components/PublicationDetailsContext';
import { openNotification } from '../../../../shared/utils/notificationsUtil';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { EnhancedPublicationPlanType } from '../../../../shared/enhancedEnums/publicationPlanType';
import { SEO } from '../../../../shared/components/SEO';
import { Heading } from '../../../../shared/components/Heading';
import { LocalizedNumber } from '../../../../shared/components/LocalizedNumber';
import { Paragraph } from '../../../../shared/components/Paragraph';
import { Link } from '../../../../shared/components/Link';
import { UpgradePublicationPlanSection } from './components/UpgradePublicationPlanSection';
import PublicationPlanTable from '../../../../shared/components/publication/PublicationPlanTable';
import { RenewPublicationPlanSection } from './components/RenewPublicationPlanSection';
import { Section } from '../../../../shared/components/Section';
import { withExpiredPublicationError } from '../../components/withExpiredPublicationError';

interface State {
  showPlanComparisonTable?: boolean;
  purchasePlanType?: PublicationPlanType;
}

export interface OnPlanPurchasedHandler {
  onPlanPurchased: (plan: PublicationPlanType) => void;
}

export interface OpenPlanPurchaseModalHandler {
  openPurchasePlanModal: (plan: PublicationPlanType) => void;
}

type Handlers = OnPlanPurchasedHandler & OpenPlanPurchaseModalHandler;

type Props = WithPublicationPlanDetailProps &
  WithPublicationDetailsContextProps &
  WithStateProps<State> &
  Handlers;

const PublicationAccountComponent: React.SFC<Props> = (props) => {
  const {
    setState,
    onPlanPurchased,
    openPurchasePlanModal,
    publicationDetail: { publication },
    state: { showPlanComparisonTable, purchasePlanType },
    planDetails
  } = props;

  const planType = EnhancedPublicationPlanType.get(planDetails.plan);
  const planName = <FormattedMessage {...planType.name} />;
  const writerLimit = <LocalizedNumber value={planType.maxWriters} />;
  const editorLimit = <LocalizedNumber value={planType.maxEditors} />;

  return (
    <React.Fragment>
      <SEO title={PublicationDetailsMessages.Account} publication={publication} />
      <Heading size={2}>
        <FormattedMessage {...PublicationDetailsMessages.Account} />
      </Heading>

      <Section
        title={<FormattedMessage {...PublicationDetailsMessages.CurrentPlan} />}
        description={<FormattedMessage
          {...PublicationDetailsMessages.CurrentPlanDescription}
          values={{planName, writerLimit, editorLimit}}
        />}
      >
        <Paragraph>
          <FormattedMessage {...(planType.isBasicPlan()
            ? PublicationDetailsMessages.NeedToUpgradeQuestion
            : PublicationDetailsMessages.DownGradeDescription)}
          />
          {' '}
          <Link.Anchor onClick={() =>
            setState(ss => ({...ss, showPlanComparisonTable: !showPlanComparisonTable}))
          }>
            <FormattedMessage {...PublicationDetailsMessages.ComparePlans} />
          </Link.Anchor>
        </Paragraph>

        {showPlanComparisonTable && <PublicationPlanTable />}
      </Section>

      <RenewPublicationPlanSection
        publication={publication}
        planDetails={planDetails}
        openPurchasePlanModal={openPurchasePlanModal}
      />

      <UpgradePublicationPlanSection
        planDetails={planDetails}
        openPurchasePlanModal={openPurchasePlanModal}
      />

      <PurchasePublicationPlanModal
        publication={publication}
        planDetails={planDetails}
        visible={purchasePlanType !== undefined}
        dismiss={() => setState(ss => ({...ss, purchasePlanType: undefined}))}
        onPlanPurchased={onPlanPurchased}
        plan={purchasePlanType}
      />
    </React.Fragment>
  );
};

type HandlerProps = WithPublicationPlanDetailProps &
  InjectedIntlProps &
  WithPublicationDetailsContextProps &
  WithStateProps<State>;

export default compose(
  withExpiredPublicationError(true),
  // jw: we need to setup the publicationOid for the request to the server for the plan details!
  withProps((props: WithPublicationDetailsContextProps) => {
    const { publicationDetail: { oid } } = props;

    return {publicationOid: oid};
  }),
  withPublicationPlanDetail,
  withLoadingPlaceholder(fullPlaceholder),
  withState<State>({}),
  injectIntl,
  withHandlers<HandlerProps, Handlers>({
    onPlanPurchased: (props: HandlerProps) =>
      async (plan: PublicationPlanType) =>
    {
      const { refetchPublicationDetail, intl: { formatMessage } } = props;

      const planType = EnhancedPublicationPlanType.get(plan);
      const planName = formatMessage(planType.name);

      // jw: first, let's render a success message so the user knows that their plan has been updated
      await openNotification.updateSuccess(
        {
          message: formatMessage(PublicationDetailsMessages.PlanPaymentReceived, {planName}),
          description: null
        });

      // jw: next, we need to refetch the publication detail, which should cause a re-render back down to here.
      if (refetchPublicationDetail) {
        refetchPublicationDetail();
      } else {
        // todo:error-handling should never get here without a refetchPublicationDetail!
      }
    },
    openPurchasePlanModal: (props: WithStateProps<State>) => (plan: PublicationPlanType) => {
      const { setState } = props;

      setState(ss => ({...ss, purchasePlanType: plan}));
    }
  })
)(PublicationAccountComponent) as React.ComponentClass<{}>;
