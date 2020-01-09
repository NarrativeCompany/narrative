import * as React from 'react';
import { LedgerEntry, WithLedgerEntriesProps } from '@narrative/shared';
import { Alert, List } from 'antd';
import { LedgerEntryMessages } from '../../i18n/LedgerEntryMessages';
import { FormattedMessage } from 'react-intl';
import styled from '../../styled/index';
import { ListProps } from 'antd/lib/list';
import { LedgerEntryListItem } from './LedgerEntryListItem';
import { compose, Omit, withProps } from 'recompose';
import { generateSkeletonListProps, renderSkeleton } from '../../utils/loadingUtils';
import {
  withLoadMoreButtonController,
  WithLoadMoreButtonControllerParentProps,
  WithLoadMoreButtonControllerProps
} from '../../containers/withLoadMoreButtonController';
import { LoadMoreButton } from '../LoadMoreButton';

const StyledLedgerEntryList = styled<ListProps>((props) => <List {...props} />)`
`;
const LedgerEntryListsContainer = styled.div`
  .ledger-entry-list-item {
    position: relative;

    &:before {
      content: ' ';
      position: absolute;
      border-left: 2px solid ${props => props.theme.borderGrey};
      left: 7px;
      top: 0;
      bottom: 0;
    }
    // jw: position the icon relative so it appears above the bar.
    img {
      position:relative;
    }
  }
  // jw: for the first item in the list, bring the bar down 25px
  // note: order matters on the :first-child and :before directives.
  .ledger-entry-list-item:first-child:before {
    top: 25px;
  }  
  // jw: for the last item in the list, ensure the bar only covers the top 25px
  // note: order matters on the :last-child and :before directives.
  .ledger-entry-list-item:last-child:before {
    bottom: calc(100% - 25px);
  }
  .ant-list-split .ant-list-item {
    border-bottom: 0;
  }
`;

interface ForProfilePageProps {
  forProfilePage?: boolean;
}

type ParentProps =
  WithLedgerEntriesProps &
  ForProfilePageProps;

type Props =
  ForProfilePageProps &
  // jw: the HOC will convert the loadMoreEntries into the loadMoreButtonProps
  WithLoadMoreButtonControllerProps &
  Omit<WithLedgerEntriesProps, 'loadMoreEntries'>;

const LedgerEntryListComponent: React.SFC<Props> = (props) => {
  const { ledgerEntriesLoading, entries, forProfilePage, loadMoreButtonProps } = props;

  if (ledgerEntriesLoading && !entries.length) {
    return <List {...generateSkeletonListProps(5, renderSkeleton)}/>;
  }

  if (!entries || !entries.length) {
    return (
      <Alert type="warning" message={<FormattedMessage {...LedgerEntryMessages.NoEntriesInHistory}  />} />
    );
  }

  return (
    <React.Fragment>
      <LedgerEntryListsContainer>
        <StyledLedgerEntryList
          className="ledger-entry-list"
          itemLayout="horizontal"
          dataSource={entries}
          renderItem={(ledgerEntry: LedgerEntry) => (
            <LedgerEntryListItem ledgerEntry={ledgerEntry} forProfilePage={forProfilePage} />
          )}
        />
      </LedgerEntryListsContainer>

      {loadMoreButtonProps && <LoadMoreButton {...loadMoreButtonProps} />}
    </React.Fragment>
  );
};

export const LedgerEntryList = compose(
  withProps<WithLoadMoreButtonControllerParentProps, ParentProps>((props) => {
    // jw: we need to convert the loadMoreEntries property into the name that the load more controller expects.
    const loadMore = props.loadMoreEntries;

    return { loadMore, loadMoreWhenViewportWithin: 800 };
  }),
  withLoadMoreButtonController
)(LedgerEntryListComponent) as React.ComponentClass<ParentProps>;

/**
 * HOC to get properties from the WithLedgerEntriesProps query result
 */

type ExportedProps = Omit<ParentProps, 'forProfilePage'>;

export interface WithLedgerEntryListPropsFromQueryProps {
  ledgerEntryListProps: ExportedProps;
}

export const withLedgerEntryListPropsFromQuery = compose(
  withProps<WithLedgerEntryListPropsFromQueryProps, WithLedgerEntriesProps>((props) => {
    const { ledgerEntriesLoading, entries, loadMoreEntries } = props;

    const ledgerEntryListProps: ExportedProps = { ledgerEntriesLoading, entries, loadMoreEntries };

    return { ledgerEntryListProps };
  })
);
