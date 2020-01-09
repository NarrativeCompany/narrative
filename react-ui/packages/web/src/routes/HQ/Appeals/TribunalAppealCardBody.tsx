import * as React from 'react';
import { compose, withProps } from 'recompose';
import { TribunalAppealReportIssueBox } from './TribunalAppealReportIssueBox';
import { TribunalAppealReportSubmitter } from './components/TribunalAppealReportSubmitter';
import {
  NicheEditDetail,
  TribunalIssue,
  TribunalIssueType,
  User,
} from '@narrative/shared';

import { Col, Row } from 'antd';
import styled from '../../../shared/styled';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';

const CardBody = styled<FlexContainerProps>(FlexContainer)`
  background-color: #FBFDFF;
  justify-content: space-between;
  border-top: 1px solid #E2E6EC;
  border-bottom: 1px solid #E2E6EC;
  width: 100%;
  padding: 20px 32px 20px 32px;

  @media screen and (max-width: 576px) {
    padding: 10px 10px 10px 10px;
    justify-content: space-between;
    align-items: center;
  }
`;

interface ParentProps {
  issue: TribunalIssue;
}

interface WithProps {
  reporter: User;
  comments: string;
  creationDatetime: string;
  type: TribunalIssueType;
  nicheEditDetail: NicheEditDetail;
}

type Props =
  WithProps &
  ParentProps;

const TribunalAppealCardBodyComponent: React.SFC<Props> = (props) => {
  const { reporter, comments, creationDatetime, nicheEditDetail, type } = props;

  return (
    <CardBody>
      <Row type="flex" justify="space-between" style={{width: '100%'}}>
        <Col span={24}>
          <TribunalAppealReportSubmitter user={reporter} creationDatetime={creationDatetime}/>
          <TribunalAppealReportIssueBox type={type} nicheEditDetail={nicheEditDetail} comments={comments}/>
        </Col>
      </Row>
    </CardBody>
  );
};

export const TribunalAppealCardBody = compose(
  withProps((props: Props) => {
    const { issue } = props;
    const { type, nicheEditDetail, lastReport } = issue;
    const { creationDatetime, reporter, comments } = lastReport;

    return { type, reporter, comments, creationDatetime, nicheEditDetail };
  })
)(TribunalAppealCardBodyComponent) as React.ComponentClass<ParentProps>;
