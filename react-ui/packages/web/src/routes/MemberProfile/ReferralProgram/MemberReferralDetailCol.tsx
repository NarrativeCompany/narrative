import * as React from 'react';
import { Col } from 'antd';
import styled from '../../../shared/styled';
import { Heading } from '../../../shared/components/Heading';

interface ParentProps {
  label: string | React.ReactNode;
}

type Props = ParentProps;

const DetailHeading = styled(Heading)`
  text-align: center;
`;

const DetailLabel = styled.div`
  text-align: center;
`;

export const MemberReferralDetailCol: React.SFC<Props> = (props) => {
  const { label } = props;

  return (
    <Col sm={8}>
      <DetailHeading size={3}>
        {props.children}
      </DetailHeading>
      <DetailLabel>{label}</DetailLabel>
    </Col>
  );
};
