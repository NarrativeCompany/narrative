import * as React from 'react';
import { compose, withProps } from 'recompose';
import { SEO } from '../../../../shared/components/SEO';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../../shared/context/MemberProfileContext';
import { MemberRewardsMessages } from '../../../../shared/i18n/MemberRewardsMessages';
import { withLoadingPlaceholder } from '../../../../shared/utils/withLoadingPlaceholder';
import { generatePath } from 'react-router';
import {
  UserTransactionsParentProps,
  WithExtractedUserRewardTransactionsProps,
  withUserRewardTransactions
} from '@narrative/shared';
import {
  WithPaginationControllerProps, withQueryParamPaginationController
} from '../../../../shared/containers/withPaginationController';
import { createUrl } from '../../../../shared/utils/routeUtils';
import { WebRoute } from '../../../../shared/constants/routes';
import { Col, Pagination, Row } from 'antd';
import styled from '../../../../shared/styled';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { EnhancedUserWalletTransactionType } from '../../../../shared/enhancedEnums/userWalletTransactionType';
import { LocalizedTime } from '../../../../shared/components/LocalizedTime';
import { NrveValue } from '../../../../shared/components/rewards/NrveValue';
import { Text } from '../../../../shared/components/Text';
import { themeColors } from '../../../../shared/styled/theme';
import { mediaQuery } from '../../../../shared/styled/utils/mediaQuery';
import { EnhancedUserWalletTransactionStatus } from '../../../../shared/enhancedEnums/userWalletTransactionStatus';
import { CancelRedemptionRequestLink } from './CancelRedemptionRequestLink';
import { RewardPeriodsConnect } from '../../../../shared/context/RewardPeriodsContext';

const pageParam = 'page';

interface ParentProps {
  onCanceledRedemptionRequest: () => void;
}

type Props =
  ParentProps &
  InjectedIntlProps &
  WithMemberProfileProps &
  WithPaginationControllerProps &
  WithExtractedUserRewardTransactionsProps;

const PaginationWrapper = styled(FlexContainer)`
  margin: 20px 0;
`;

const TransactionAmountCol = styled(Col)`
  text-align: right;
  vertical-align: top;
  ${mediaQuery.sm_down`
    text-align: left;
  `};
`;

const MemberRewardsTransactionsComponent: React.SFC<Props> = (props) => {
  const {
    intl: { formatMessage },
    detailsForProfile: { user: { displayName } },
    transactions,
    pagination,
    onCanceledRedemptionRequest
  } = props;

  return (
    <React.Fragment>
      <SEO title={formatMessage(MemberRewardsMessages.TransactionsSeoTitle, {displayName})} />
      {transactions.map(transaction => {
        const { transactionDatetime, type, status } = transaction;

        const transactionType = EnhancedUserWalletTransactionType.get(type);
        const transactionStatus = EnhancedUserWalletTransactionStatus.get(status);
        const isNegative = transaction.amount.nrve.startsWith('-');

        return (
          <Row
            key={transaction.oid}
            gutter={16}
            style={{fontSize: 16, marginBottom: 10, opacity: transactionStatus.opacity}}
          >
            <Col md={5}>
              <LocalizedTime time={transactionDatetime} dateOnly={true} />
            </Col>
            <Col md={13}>
              {transactionType.getMessage(transaction)}
            </Col>
            <TransactionAmountCol md={6}>
              <Text style={{color: isNegative ? themeColors.primaryRed : themeColors.primaryGreen}}>
                <NrveValue value={transaction.amount} showFullDecimal={true}/>
              </Text>
              {!transactionStatus.isCompleted() && (
                <React.Fragment>
                  <br/>
                  <Text size="small">
                    <FormattedMessage {...transactionStatus.message}/>
                  </Text>
                </React.Fragment>
              )}
              {transactionStatus.isPending() && (
                <React.Fragment>
                  {' '}&middot;{' '}
                  <CancelRedemptionRequestLink
                    transaction={transaction}
                    onCanceledRedemptionRequest={onCanceledRedemptionRequest}
                  />
                </React.Fragment>
              )}
            </TransactionAmountCol>
          </Row>
        );
      })}
      <PaginationWrapper centerAll={true}>
        <Pagination {...pagination} />
      </PaginationWrapper>
    </React.Fragment>
  );
};

export default compose(
  MemberProfileConnect,
  RewardPeriodsConnect,
  injectIntl,
  withProps((props: WithMemberProfileProps): Pick<UserTransactionsParentProps, 'userOid'> => {
    const { detailsForProfile: { user: { oid: userOid } } } = props;

    return { userOid };
  }),
  withQueryParamPaginationController<WithExtractedUserRewardTransactionsProps>(
    withUserRewardTransactions,
    // tslint:disable-next-line:no-any
    (props: any) => {
      const { detailsForProfile: { user: { username } } } = props;

      return createUrl(generatePath(WebRoute.UserProfileRewardsTransactions, { username }));
    },
    pageParam
  ),
  withUserRewardTransactions,
  withLoadingPlaceholder()
)(MemberRewardsTransactionsComponent) as React.ComponentClass<ParentProps>;
