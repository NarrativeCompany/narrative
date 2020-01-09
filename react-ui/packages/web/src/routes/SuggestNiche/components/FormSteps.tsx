import * as React from 'react';
import { Col, Row, Steps as AntSteps } from 'antd';
import { StepsProps } from 'antd/lib/steps';
import { CustomIcon } from '../../../shared/components/CustomIcon';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import styled from '../../../shared/styled/index';

const IconWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 10px;
`;

interface NicheStep {
  title: string;
}

const suggestNicheSteps: NicheStep[] = [
  {title: 'Niche name'},
  {title: 'Niche description'},
  { title: 'Confirm Niche' },
];

const Steps = styled<StepsProps>(({...props}) => <AntSteps {...props}/>)`
  &.ant-steps {
    margin-top: 20px;
    justify-content: center;
  }
  
  &.ant-steps-horizontal {
    display: flex !important;
    align-items: center;
    
    .ant-steps-item-tail {
      display: none !important;
    }
    
    .ant-steps-item-content {
      min-height: auto !important;
    }
  }
  
  .ant-steps-item {
    flex: none;
    margin-right: 0 !important;
    display: flex;
    align-items: center;
    
    &.ant-steps-item-process {
      .ant-steps-item-icon {
        background-color: ${props => props.theme.secondaryBlue};
        border-color: ${props => props.theme.secondaryBlue};
      }
    }
    
    &.ant-steps-item-wait,
    &.ant-steps-item-finish {
      .ant-steps-item-icon {
        width: 20px;
        height: 20px;
        background-color: ${props => props.theme.borderGrey};
        border-color: ${props => props.theme.borderGrey};
      }
      
      .ant-steps-icon {
        display: none;
      }
    }
    
    &:last-child {
      .ant-steps-item-icon {
        margin-right: 0;
      }
    }
  }
`;

interface Props {
  current: number;
  stepsContent: React.ReactNode;
}

export const FormSteps: React.SFC<Props> = (props) => {
  const { current } = props;

  return (
    <React.Fragment>
      {current < 3 &&
      <Row gutter={16}>
        <Col xs={24} sm={{span: 18, offset: 3}} md={{span: 10, offset: 7}}>
          <IconWrapper centerAll={true}>
            <CustomIcon type="suggested"/>
          </IconWrapper>

          <FlexContainer justifyContent="center">
            <Steps current={props.current} size="small" direction="horizontal">
              {suggestNicheSteps.map(item => <AntSteps.Step key={item.title}/>)}
            </Steps>
          </FlexContainer>
        </Col>
      </Row>}

      <Row gutter={16} justify="center">
        <Col sm={24} md={{span: 20, offset: 2}}>
          {props.stepsContent}
        </Col>
      </Row>
    </React.Fragment>
  );
};
