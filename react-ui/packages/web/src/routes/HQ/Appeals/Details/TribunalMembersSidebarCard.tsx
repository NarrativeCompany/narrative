import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  User,
  withAllTribunalMembers,
  WithAllTribunalMembersProps
} from '@narrative/shared';
import { SidebarCard } from '../../../../shared/components/SidebarCard';
import { SidebarMessages } from '../../../../shared/i18n/SidebarMessages';
import { FormattedMessage } from 'react-intl';
import { List } from 'antd';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { MemberLink } from '../../../../shared/components/user/MemberLink';
import styled from 'styled-components';
import { MemberAvatar, MemberAvatarProps } from '../../../../shared/components/user/MemberAvatar';
import { ListProps } from 'antd/lib/list';

const StyledAvatar = styled<MemberAvatarProps>(MemberAvatar)`
  &.ant-avatar {
    margin-bottom: 5px;

    width: 100%;
    height: 100%;
  }
`;

const StyledList = styled<ListProps>(List)`
  .ant-row {
    display: flex;
    flex-wrap: wrap;
  }
`;

interface Props {
  members: User[];
  loading: boolean;
}

const TribunalMembersSidebarCardComponent: React.SFC<Props> = (props) => {
  const { loading, members } = props;
  const title = <FormattedMessage {...SidebarMessages.TribunalMembers} />;

  if (loading) {
    return <SidebarCard loading={true} title={title} />;
  }

  return (
    <SidebarCard title={title}>
      <StyledList
        grid={{gutter: 16, column: 3}}
        dataSource={members}
        renderItem={(member: User) => (
          <List.Item>
            <FlexContainer column={true} centerAll={true}>
              <StyledAvatar user={member} />
              <MemberLink
                user={member}
                size="small"
                style={{textAlign: 'center'}}
              />
            </FlexContainer>
          </List.Item>
        )}
      />
    </SidebarCard>
  );
};

export const TribunalMembersSidebarCard = compose(
  withAllTribunalMembers,
  withProps((props: WithAllTribunalMembersProps) => {
    const { allTribunalMembersData } = props;

    const loading =
      allTribunalMembersData &&
      allTribunalMembersData.loading;
    const members =
      allTribunalMembersData &&
      allTribunalMembersData.getAllTribunalMembers || [];

    return { members, loading };
  })
)(TribunalMembersSidebarCardComponent) as React.ComponentClass<{}>;
