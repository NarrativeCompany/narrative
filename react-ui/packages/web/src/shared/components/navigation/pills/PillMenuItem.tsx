import * as React from 'react';
import { Omit } from 'recompose';
import { FormattedMessage } from 'react-intl';
import styled from '../../../styled';
import { Icon, IconProps, SvgComponent } from '../../Icon';
import { Text } from '../../Text';
import { Link } from '../../Link';
import { mediaQuery } from '../../../styled/utils/mediaQuery';

// jw: theme is a induced property from styled, and conflicts with the one on antd.Icon. Since we don't need it lets
//     just remove it.
const PillIcon = styled<Omit<IconProps, 'theme'>>(Icon)`
  margin-right: 7px;
  ${mediaQuery.hide_xs};
`;

const SelectedPill = styled.span`
  display: inline-block;
  padding: 0 15px;
  color: white;
  background-color: ${p => p.theme.secondaryBlue};
  border-radius: 1.5em;
`;

export interface PillMenuItemProps {
  title: FormattedMessage.MessageDescriptor;
  path: string;
  selected?: boolean;
  noFollow?: boolean;
  icon?: SvgComponent;
}

export const PillMenuItem: React.SFC<PillMenuItemProps> = (props) => {
  const { title, selected, icon, noFollow, path } = props;

  // jw: capturing this up front, since we will be adding icons to the front at some point.
  const pillBody = (
    <Text size="large" color="inherit">
      {icon && <PillIcon theme={undefined} svgIcon={icon} />}
      <FormattedMessage {...title}/>
    </Text>
  );

  if (selected) {
    return <SelectedPill>{pillBody}</SelectedPill>;
  }

  return (
    <Link to={path} color="dark" size="large" noFollow={noFollow}>
      {pillBody}
    </Link>
  );
};
