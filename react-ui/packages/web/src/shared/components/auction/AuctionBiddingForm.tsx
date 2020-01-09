import * as React from 'react';
import { compose } from 'recompose';
import { Form, FormikProps, withFormik } from 'formik';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { Col, Row } from 'antd';
import { Heading } from '../Heading';
import { NRVE } from '../NRVE';
import { FlexContainer } from '../../styled/shared/containers';
import { Button } from '../Button';
import { FormMethodError } from '../FormMethodError';
import { AuctionDetailProps } from '../../../routes/HQ/Auctions/Details/AuctionDetails';
import { AuctionDetailsMessages } from '../../i18n/AuctionDetailsMessages';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from '../../containers/withExtractedCurrentUser';
import {
  applyExceptionToState,
  bidOnAuctionFormUtil,
  BidOnNichesRevokeReason,
  BidStatus,
  initialFormState,
  MethodError,
  NicheAuctionBidInput,
  NrveUsdPriceInput,
  NrveUsdValue,
  omitProperties,
  PostBidOnAuctionFormValues,
  withPostBidOnAuction,
  WithPostBidOnAuctionProps,
  withState,
  WithStateProps
} from '@narrative/shared';
import { WithUpdateAuctionDetailHandler } from '../../containers/withUpdateableAuctionDetail';
import { NrveInput } from '../NrveInput';
import { UsdValue } from '../rewards/UsdValue';

type ParentProps =
  AuctionDetailProps &
  WithUpdateAuctionDetailHandler;

type WithFormikProps =
  WithPostBidOnAuctionProps &
  ParentProps &
  WithStateProps<MethodError> &
  FormikProps<PostBidOnAuctionFormValues>;

type Props =
  AuctionDetailProps &
  WithExtractedCurrentUserProps &
  InjectedIntlProps &
  WithStateProps<MethodError> &
  FormikProps<PostBidOnAuctionFormValues>;

const AuctionBiddingFormComponent: React.SFC<Props> = (props) => {
  const {
    intl: { formatMessage },
    auction,
    currentUserLatestBidStatus,
    currentUserGlobalPermissions,
    state,
    isSubmitting,
    errors,
    values
  } = props;

  // jw: let's determine if they are the leading bidder, or if they have the right to bid. This will help in a second.
  const bidOnNichePermission =
    currentUserGlobalPermissions &&
    currentUserGlobalPermissions.bidOnNiches;
  const hasBiddingPermissions =
    bidOnNichePermission &&
    bidOnNichePermission.granted;

  // jw: let's keep this simple, prime the disable form baswed on the users base bidding permission
  let disableForm = !hasBiddingPermissions;

  // jw: now, there are two reasons that we will allow them to use the form despite the global permission
  if (bidOnNichePermission && disableForm) {
    // jw: if they do not have the right because their niche slots are full, then allow them to access as long as
    //     they are the leading bidder.
    if (bidOnNichePermission.revokeReason === BidOnNichesRevokeReason.NICHE_SLOTS_FULL) {
      disableForm = currentUserLatestBidStatus !== BidStatus.LEADING;

    // jw: if they do not have the right because they need to make a security deposit, then let's see if they bypass
    //     that requirement
    } else if (bidOnNichePermission.revokeReason === BidOnNichesRevokeReason.SECURITY_DEPOSIT_REQUIRED) {
      const { currentUserBypassesSecurityDepositRequirement } = props;

      disableForm = !currentUserBypassesSecurityDepositRequirement;
    }
  }

  // jw: if the user is not the leading bidder, and does not have the right to bid then we should not allow them to
  //     use the form. This can happen if the user is actively bidding on this auction, but has no other slots free.

  const currentBidHeading = auction.leadingBid ?
    AuctionDetailsMessages.CurrentBid :
    AuctionDetailsMessages.MinimumBid;

  const bidNrveUsdValue: NrveUsdValue = auction.leadingBid ? auction.leadingBid.bidAmount : auction.startingBid;

  return (
    <Form>
      <Row gutter={26}>
        <Col md={8}>
          <Heading size={6} uppercase={true}>
            <FormattedMessage {...currentBidHeading} />
          </Heading>

          <Heading size={4} weight={300}>
            <NRVE amount={bidNrveUsdValue.nrve} />
            <br/>
            <UsdValue nrveUsdValue={bidNrveUsdValue} size="default"/>
          </Heading>
        </Col>

        <Col md={10}>
          <Heading size={6} uppercase={true}>
            <FormattedMessage {...AuctionDetailsMessages.MakeBid} />
          </Heading>

          <FormMethodError methodError={state.methodError}/>

          <NrveInput
            placeholder={formatMessage(AuctionDetailsMessages.MaxNrveBidPlaceholder)}
            name="maxNrveBid"
            style={{marginBottom: 0}}
            autoFocus={true}
            errorCanContainHtml={true}
            errors={errors}
            values={values}
            disabled={disableForm}
            nrveUsdPrice={auction.nrveUsdPrice.nrveUsdPrice}
            showUsdOnly={true}
          />
        </Col>

        <Col md={6}>
          <FlexContainer justifyContent="center">
            <Button
              type="primary"
              style={{marginTop: 20}}
              htmlType="submit"
              disabled={disableForm}
              loading={isSubmitting}
            >
              <FormattedMessage {...AuctionDetailsMessages.PlaceBid}/>
            </Button>
          </FlexContainer>
        </Col>
      </Row>
    </Form>
  );
};

export const AuctionBiddingForm = compose(
  withExtractedCurrentUser,
  withPostBidOnAuction,
  withState<MethodError>(initialFormState),
  withFormik<WithFormikProps, PostBidOnAuctionFormValues>({
    ...bidOnAuctionFormUtil,
    mapPropsToValues: (props: AuctionDetailProps) => {
      const { currentUserLatestMaxNrveBid, currentUserLatestBidStatus } = props;

      const maxNrveBid: string = currentUserLatestMaxNrveBid && currentUserLatestBidStatus === BidStatus.LEADING
        ? currentUserLatestMaxNrveBid.nrve
        : '';

      return { maxNrveBid };
    },
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { setState, postBidOnAuction, auction, handleNewAuctionDetail, isSubmitting } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        const nrveUsdPrice = omitProperties(auction.nrveUsdPrice, ['__typename']) as NrveUsdPriceInput;

        const input: NicheAuctionBidInput = { ...values, nrveUsdPrice };
        const auctionOid = auction.oid;
        const auctionDetail = await postBidOnAuction({input, auctionOid});

        if (handleNewAuctionDetail) {
          handleNewAuctionDetail(auctionDetail, true);
        }

      } catch (err) {
        applyExceptionToState(err, setErrors, setState);
      }

      setSubmitting(false);
    }
  }),
  injectIntl
)(AuctionBiddingFormComponent) as React.ComponentClass<ParentProps>;
