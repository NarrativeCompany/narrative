import * as React from 'react';
import { compose } from 'recompose';
import { Col, Row } from 'antd';
import { FormattedMessage } from 'react-intl';
import { NomineeCard } from './NomineeCard';
import { CurrentUserNominee } from './CurrentUserNominee';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { Button } from '../../../../shared/components/Button';
import { ColProps } from 'antd/lib/grid';
import { ElectionNominee } from '@narrative/shared';
import { SharedComponentMessages } from '../../../../shared/i18n/SharedComponentMessages';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../../shared/containers/withExtractedCurrentUser';
import styled from '../../../../shared/styled';

const colProps = { xl: 6, lg: 8, md: 12, sm: 12, xs: 24 };

const StyledCol = styled<ColProps>((props) => <Col {...props}/>)`
  margin-bottom: 30px;
  
  @media screen and (max-width: 633px) {
    margin-bottom: 30px;
  }
`;

interface ParentProps {
  electionOid: string;
  electionNominees: ElectionNominee[];
  currentUserNominee: ElectionNominee | null;
  hasMoreItems: boolean;
  onFetchMore: () => void;
  isFetchingMore: boolean;
}

type Props =
  ParentProps &
  WithExtractedCurrentUserProps;

export const ModeratorNomineesListComponent: React.SFC<Props> = (props) => {
  const {
    electionNominees,
    electionOid,
    currentUserNominee,
    onFetchMore,
    hasMoreItems,
    isFetchingMore,
    currentUser
  } = props;

  return (
    <React.Fragment>
      <Row style={{marginTop: 50}} gutter={32}>
        {currentUser &&
        <StyledCol {...colProps}>
          <CurrentUserNominee electionOid={electionOid} currentUserNominee={currentUserNominee}/>
        </StyledCol>}

        {electionNominees.map(nominee => (
          <StyledCol key={nominee.oid} {...colProps}>
            <NomineeCard nominee={nominee} electionOid={electionOid}/>
          </StyledCol>
        ))}
      </Row>

      {hasMoreItems &&
      <FlexContainer centerAll={true}>
        <Button size="large" onClick={onFetchMore} type="primary" loading={isFetchingMore}>
          <FormattedMessage {...SharedComponentMessages.LoadMore} />
        </Button>
      </FlexContainer>}
    </React.Fragment>
  );
};

export const ModeratorNomineesList = compose(
  withExtractedCurrentUser
)(ModeratorNomineesListComponent) as React.ComponentClass<ParentProps>;
