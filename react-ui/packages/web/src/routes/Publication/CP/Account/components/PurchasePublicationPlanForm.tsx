import * as React from 'react';
import { compose, lifecycle, withHandlers } from 'recompose';
import {
  Publication,
  PublicationPlanDetail,
  PublicationPlanType,
  InvoiceDetail,
  PublicationInvoice,
  withState,
  withCreatePublicationInvoice,
  WithStateProps,
  WithCreatePublicationInvoiceProps,
  handleFormlessServerOperation,
  InvoiceStatus
} from '@narrative/shared';
import { ContainedLoading } from '../../../../../shared/components/Loading';
import { InvoicePaymentOptions } from '../../../../HQ/Auctions/Invoice/InvoicePaymentOptions';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { OnPlanPurchasedHandler } from '../PublicationAccount';
import { PurchasePublicationPlanFormDescription } from './PurchasePublicationPlanFormHeader';

interface State {
  publicationInvoice?: PublicationInvoice;
}

interface Handlers {
  handleInvoiceUpdate: (invoice: InvoiceDetail) => void;
}

interface ParentProps extends OnPlanPurchasedHandler {
  publication: Publication;
  planDetails: PublicationPlanDetail;
  plan: PublicationPlanType;
  dismissModal: () => void;
}

type Props = WithStateProps<State> &
  InjectedIntlProps &
  ParentProps &
  Handlers;

const PurchasePublicationPlanFormComponent: React.SFC<Props> = (props) => {
  const {
    state: { publicationInvoice },
    handleInvoiceUpdate,
    intl: { formatMessage },
    publication,
    planDetails,
    plan
  } = props;

  if (!publicationInvoice) {
    return <ContainedLoading />;
  }

  if (publicationInvoice.invoiceDetail.status === InvoiceStatus.INVOICED) {
    const { name } = publication;
    return (
      <React.Fragment>
        <PurchasePublicationPlanFormDescription
          planDetails={planDetails}
          publicationInvoice={publicationInvoice}
        />
        <InvoicePaymentOptions
          invoice={publicationInvoice.invoiceDetail}
          handleInvoiceUpdate={handleInvoiceUpdate}
          cardPaymentDescription={formatMessage(PublicationDetailsMessages.PublicationCardDescription, {name})}
          cardCustomPaymentData={`publication/${publication.oid}/plan/${plan.toLowerCase()}`}
        />
      </React.Fragment>
    );
  }

  return null;
};

export const PurchasePublicationPlanForm = compose(
  withState<State>({}),
  withCreatePublicationInvoice,
  injectIntl,
  lifecycle<WithStateProps<State> & WithCreatePublicationInvoiceProps & ParentProps, {}>({
    // tslint:disable-next-line object-literal-shorthand
    componentDidMount: async function() {
      const { state: { publicationInvoice } } = this.props;

      // jw: if we have already set an invoice then short out.
      if (publicationInvoice) {
        return;
      }

      // jw: guess we are going to have to go to the server to create a new invoice, and then cache it within our state
      //     for rendering purposes.
      const { publication, plan, setState, createPublicationInvoice } = this.props;

      const result: PublicationInvoice | null = await handleFormlessServerOperation(
        () => createPublicationInvoice( {plan}, publication.oid)
      );

      if (result) {
        setState(ss => ({...ss, publicationInvoice: result}));
      }
    }
  }),
  withHandlers({
    handleInvoiceUpdate: (props: ParentProps & WithStateProps<State>) => (invoiceDetail: InvoiceDetail) => {
      const { setState, dismissModal, onPlanPurchased, plan, state: { publicationInvoice } } = props;

      // jw: if the invoice is paid we need to close the modal and report back with the new plan.
      if (invoiceDetail.status === InvoiceStatus.PAID) {
        dismissModal();
        onPlanPurchased(plan);
        // jw: now exit out, since we do not want to change our state and cause a re-render.
        return;
      }

      if (!publicationInvoice) {
        // todo:error-handling: We should always have a publicationInvoice by this point, so what happened?
        return;
      }

      const newPublicationInvoice: PublicationInvoice = {
        ...publicationInvoice,
          invoiceDetail
      };

      // jw: replace the invoice into the publicationInvoice object that is stored in state.
      setState(ss => ({...ss, publicationInvoice: newPublicationInvoice}));
    }
  })
)(PurchasePublicationPlanFormComponent) as React.ComponentClass<ParentProps>;
