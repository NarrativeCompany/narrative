import * as React from 'react';
import { Icon } from 'antd';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import styled from '../../../shared/styled';
import { Link } from '../../../shared/components/Link';

interface WrapperProps extends FlexContainerProps {
  isLinked: boolean;
}
const ApprovalActionWrapper = styled<WrapperProps>(
  ({isLinked, ...flexProps}) => <FlexContainer {...flexProps} />
)`
  flex: 1;
  margin-top: 15px;
  min-height: 30px;
  
  span.action-text {
    margin-left: 5px;
    line-height: 11px;
    font-size: ${props => props.theme.textFontSizeSmall};
    color: ${props => props.isLinked ? 'inherit' : props.theme.textColor};
  }
`;

interface ParentProps {
  href?: string;
  iconType: string;
  children: React.ReactNode;
}

export const ApprovalAction: React.SFC<ParentProps> = (props) => {
  const { href, iconType, children } = props;

  const content = (
    <React.Fragment>
      <Icon type={iconType}/>
      <span className="action-text">{children}</span>
    </React.Fragment>
  );

  return (
    <ApprovalActionWrapper centerAll={true} isLinked={!!href}>
      {href
        ? <Link to={href}>{content}</Link>
        : content
      }

    </ApprovalActionWrapper>
  );
};
