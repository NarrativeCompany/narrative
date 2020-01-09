import * as React from 'react';
import { compose } from 'recompose';
import { List } from 'antd';
import { ApprovalCard } from './ApprovalCard';
import { NicheList } from '../components/NicheList';
import { WebRoute } from '../../../shared/constants/routes';
import { PageHeader } from '../../../shared/components/PageHeader';
import { SEO } from '../../../shared/components/SEO';
import { NotFound } from '../../../shared/components/NotFound';
import { generateSkeletonListProps, renderLoadingCard } from '../../../shared/utils/loadingUtils';
import { GuidelinesModal } from '../components/GuidelinesModal';
import { SuggestNicheButton } from '../components/SuggestNicheButton';
import { Link } from '../../../shared/components/Link';
import { FormattedMessage } from 'react-intl';
import { ApprovalListPageMessages } from '../../../shared/i18n/ApprovalListPageMessages';
import {
  Referendum,
  withAllBallotBoxReferendums,
  WithAllBallotBoxReferendumsProps,
  withState,
  WithStateProps
} from '@narrative/shared';
import {
  withPaginationController,
  WithPaginationControllerProps
} from '../../../shared/containers/withPaginationController';
import { SEOMessages } from '../../../shared/i18n/SEOMessages';

interface State {
  isGuidelinesModalVisible: boolean;
}

const initialState: State = {
  isGuidelinesModalVisible: false,
};

type Props =
  WithStateProps<State> &
  WithAllBallotBoxReferendumsProps &
  WithPaginationControllerProps;

const ApprovalListPageComponent: React.SFC<Props> = (props) => {
  const { referendums, loading, pagination, pageSize, state, setState } = props;

  const TitleHelper = (
    <Link.Anchor
      textDecoration="underline"
      onClick={() => setState(ss => ({ ...ss, isGuidelinesModalVisible: true }))}
    >
      <FormattedMessage {...ApprovalListPageMessages.PageHeaderTitleHelper}/>
    </Link.Anchor>
  );

  let ListContent;

  if (loading) {
    ListContent = (
      <NicheList {...generateSkeletonListProps(pageSize, renderLoadingCard)}/>
    );
  } else if (!referendums || !referendums.length) {
    ListContent = <NotFound/>;
  } else {
    ListContent = (
      <NicheList
        dataSource={referendums}
        pagination={pagination}
        renderItem={(referendum: Referendum) => (
          <List.Item key={referendum.oid}>
            <ApprovalCard referendum={referendum}/>
          </List.Item>
        )}
      />
    );
  }

  return (
    <React.Fragment>
      <SEO
        title={SEOMessages.ApprovalsTitle}
        description={SEOMessages.ApprovalsDescription}

      />
      <PageHeader
        preTitle={<FormattedMessage {...ApprovalListPageMessages.PageHeaderPreTitle}/>}
        title={<FormattedMessage {...ApprovalListPageMessages.PageHeaderTitle}/>}
        titleHelper={TitleHelper}
        description={<FormattedMessage {...ApprovalListPageMessages.PageHeaderDescription}/>}
        extra={<SuggestNicheButton/>}
        iconType="review"
      />
      {ListContent}

      <GuidelinesModal
        dismiss={() => setState(ss => ({ ...ss, isGuidelinesModalVisible: false }))}
        visible={state.isGuidelinesModalVisible}
      />
    </React.Fragment>
  );
};

export const ApprovalListPage = compose(
  withState(initialState),
  withPaginationController<WithAllBallotBoxReferendumsProps>(
    withAllBallotBoxReferendums,
    WebRoute.Approvals
  )
)(ApprovalListPageComponent) as React.ComponentClass<{}>;
