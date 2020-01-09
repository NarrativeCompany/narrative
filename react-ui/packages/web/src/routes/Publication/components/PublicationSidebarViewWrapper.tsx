import * as React from 'react';
import { Col, Row } from 'antd';
import { SidebarCol } from '../../../shared/components/SidebarViewWrapper';

interface Props {
  sidebarItems: React.ReactNode;
}

export const PublicationSidebarViewWrapper: React.SFC<Props> = (props) => {
  const { children, sidebarItems } = props;

  return (
    <Row gutter={24}>
      <Col lg={18}>
        {children}
      </Col>
      <SidebarCol lg={6}>
        {sidebarItems}
      </SidebarCol>
    </Row>
  );
};
