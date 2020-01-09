import * as React from 'react';
import { NicheEditDetail } from '@narrative/shared';
import { Col, Row } from 'antd';
import { Heading } from '../../../../shared/components/Heading';
import { FormattedMessage } from 'react-intl';
import { TribunalAppealCardIssueBoxMessages } from '../../../../shared/i18n/TribunalAppealCardIssueBoxMessages';
import { NicheEditDetailPane } from './NicheEditDetailPane';

interface Props {
  editDetails: NicheEditDetail;
  currentVersionFooter?: React.ReactNode;
  newVersionFooter?: React.ReactNode;
}

export const NicheEditDetails: React.SFC<Props> = (props) => {
  const { currentVersionFooter, newVersionFooter } = props;
  const { originalName, originalDescription, newName, newDescription } = props.editDetails;

  // jw: If a new name or description were not specified, then default to the original since it did not change.
  const newNameResolved = newName || originalName;
  const newDescriptionResolved = newDescription || originalDescription;

  return (
    <Row gutter={16}>
      <Col md={12}>
        <Heading size={5}>
          <FormattedMessage {...TribunalAppealCardIssueBoxMessages.CurrentVersion}/>
        </Heading>
        <NicheEditDetailPane
          name={originalName}
          description={originalDescription}
          isNewVersion={false}
        />
        {currentVersionFooter}
      </Col>
      <Col md={12}>
        <Heading size={5}>
          <FormattedMessage {...TribunalAppealCardIssueBoxMessages.NewVersion}/>
        </Heading>
        <NicheEditDetailPane
          name={newNameResolved}
          description={newDescriptionResolved}
          isNewVersion={true}
        />
        {newVersionFooter}
      </Col>
    </Row>
  );
};
