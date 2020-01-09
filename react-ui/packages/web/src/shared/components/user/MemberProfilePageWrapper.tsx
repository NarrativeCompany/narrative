import * as React from 'react';
import { UserDetail } from '@narrative/shared';
import { Row, Col } from 'antd';
import { MemberProfilePageDetailsCard } from './MemberProfilePageDetailsCard';
import { Card, CardProps } from '../Card';

interface ParentProps {
  userDetail: UserDetail;
  isCurrentUser: boolean;
  cardProps?: CardProps;
}

type Props = ParentProps;

export const MemberProfilePageWrapper: React.SFC<Props> = (props) => {
  const { userDetail, isCurrentUser, cardProps } = props;

  if (userDetail == null) {
    // jw: This should never happen since the route should ensure that we have a user
    //     before it ever takes the user here.
    throw new Error('MemberProfilePageWrapper: missing user');
  }

  return (
    <Row gutter={16}>
      <Col md={6}>
        <MemberProfilePageDetailsCard
          userDetail={userDetail}
          isCurrentUser={isCurrentUser}
        />
      </Col>

      <Col md={18}>
        <Card {...cardProps}>
          {props.children}
        </Card>
      </Col>
    </Row>
  );
};
