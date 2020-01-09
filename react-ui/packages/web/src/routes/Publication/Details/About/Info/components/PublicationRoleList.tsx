import * as React from 'react';
import { compose } from 'recompose';
import { User } from '@narrative/shared';
import { WithPublicationDetailsContextProps } from '../../../../components/PublicationDetailsContext';
import { MemberAvatar } from '../../../../../../shared/components/user/MemberAvatar';
import styled from '../../../../../../shared/styled';
import { ListProps } from 'antd/lib/list';
import { List } from 'antd';
import { FlexContainer } from '../../../../../../shared/styled/shared/containers';
import { MemberLink } from '../../../../../../shared/components/user/MemberLink';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { SectionHeader } from '../../../../../../shared/components/SectionHeader';
import { SettingsGroup } from '../../../../../MemberCP/settingsStyles';

const StyledList = styled<ListProps>(List)`
  .ant-row {
    display: flex;
    flex-wrap: wrap;
  }
`;

interface ParentProps {
  title: FormattedMessage.MessageDescriptor;
  users: User[];
  excludeUserCount?: boolean;
  style?: React.CSSProperties;
  className?: string;
}

type Props =
  WithPublicationDetailsContextProps &
  ParentProps &
  InjectedIntlProps;

const PublicationRoleListComponent: React.SFC<Props> = (props) => {
  const { title, users, excludeUserCount, intl, style, className } = props;

  let listTitle = intl.formatMessage(title);

  if (!excludeUserCount) {
    listTitle += ' (' + users.length + ')';
  }

  return (
    <SettingsGroup className={className} style={style}>
      <SectionHeader title={listTitle}/>
      <StyledList
        grid={{xs: 2, md: 3, lg: 4}}
        dataSource={users}
        renderItem={(user: User) => (
          <List.Item>
            <FlexContainer style={{alignItems: 'center'}}>
              <MemberAvatar user={user} />
              <MemberLink
                style={{marginLeft: 10}}
                user={user}
                hideBadge={true}
              />
            </FlexContainer>
          </List.Item>
        )}
      />
    </SettingsGroup>
  );
};

export const PublicationRoleList = compose(
  injectIntl
)(PublicationRoleListComponent) as React.ComponentClass<ParentProps>;
