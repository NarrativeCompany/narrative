import * as React from 'react';
import { Col, Icon, Row } from 'antd';
import { StepWrapper } from './FormStepWrapper';
import { FormButtonGroup } from '../../../shared/components/FormButtonGroup';
import { SimilarNicheCard } from './SimilarNicheCard';
import { FormattedMessage } from 'react-intl';
import { Niche } from '@narrative/shared';
import { Heading } from '../../../shared/components/Heading';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { SimilarNicheResultsStepMessages } from '../../../shared/i18n/SimilarNicheResultsStepMessages';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import styled from '../../../shared/styled';

const NoConflictsWrapper = styled<FlexContainerProps>(FlexContainer)`
  min-height: 250px;
`;

// tslint:disable no-any
interface ParentProps {
  onClick: () => any;
  onPrevClick: () => any;
  similarNiches?: Niche[];
}
// tslint:enable no-any

export const SimilarNicheResultsStep: React.SFC<ParentProps> = (props) => {
  const { onClick, onPrevClick, similarNiches } = props;

  const ReviewContent = !similarNiches || !similarNiches.length ? (
    <NoConflictsWrapper centerAll={true}>
      <Heading size={3} weight={300}>
        <FormattedMessage {...SimilarNicheResultsStepMessages.NoSimilarNichesText}/>
      </Heading>
    </NoConflictsWrapper>
  ) : (
    <React.Fragment>
      {similarNiches.map(niche => (
        <SimilarNicheCard key={niche.oid} niche={niche}/>
      ))}
    </React.Fragment>
  );

  const BackBtn = (
    <React.Fragment>
      <Icon type="left"/> <FormattedMessage {...SimilarNicheResultsStepMessages.BackBtnText}/>
    </React.Fragment>
  );

  return (
    <StepWrapper
      title={<FormattedMessage {...SimilarNicheResultsStepMessages.PageHeaderTitle}/>}
      description={<FormattedMessage {...SimilarNicheResultsStepMessages.PageHeaderDescription}/>}
    >
      <Row gutter={16}>
        <Col span={24}>
          {ReviewContent}
        </Col>

        <Col span={24}>
          <FormButtonGroup
            btnText={<FormattedMessage {...SharedComponentMessages.NextBtnText}/>}
            linkText={BackBtn}
            btnProps={{style: {minWidth: 180}, onClick}}
            linkProps={{onClick: onPrevClick}}
          />
        </Col>
      </Row>
    </StepWrapper>
  );
};
