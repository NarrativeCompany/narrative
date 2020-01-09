import * as React from 'react';
import { Col, Row } from 'antd';
import { ViewWrapper, ViewWrapperProps } from './ViewWrapper';
import styled from '../styled';
import { ColProps } from 'antd/lib/grid';
import { mediaQuery } from '../styled/utils/mediaQuery';

/**
 * SIDEBAR PROPERTIES / COMPONENTS
 */

interface HideSidebarProp {
  includeSidebarOnLgDown?: boolean;
}

interface SidebarProps extends HideSidebarProp {
  sidebarItems?: React.ReactNode;
  sidebarPlacement?: 'left';
}

export const SidebarCol = styled<ColProps & HideSidebarProp>
(({includeSidebarOnLgDown, ...rest}) => <Col {...rest} />)`
  ${p => !p.includeSidebarOnLgDown && mediaQuery.hide_md_down}
`;

const ViewRow = styled(Row)`
  ${mediaQuery.lg_up`
    & > div {
      /* 
        jw: let's distrubute the gutter padding between both sides of the column so that one column does not take the
            full weight of the distance.
      */
      &:not(:first-child) {
        padding-left: 8px;
      }
      &:not(:last-child) {
        padding-right: 8px;
      }
    }
`};
`;

const SidebarViewBody: React.SFC<SidebarProps> = (props) => {
  const { sidebarItems, children } = props;

  if (!sidebarItems) {
    return (
      <React.Fragment>
        {children}
      </React.Fragment>
    );
  }

  const { sidebarPlacement, includeSidebarOnLgDown } = props;

  const sidebarColumn = (
    <SidebarCol lg={6} includeSidebarOnLgDown={includeSidebarOnLgDown}>
      {sidebarItems}
    </SidebarCol>
  );

  return (
    <ViewRow style={{width: '100%'}}>
      {sidebarPlacement === 'left' && sidebarColumn}

      <Col lg={18}>
        {children}
      </Col>

      {!sidebarPlacement && sidebarColumn}
    </ViewRow>
  );
};

/**
 * VIEW_WRAPPER PROPERTIES / COMPONENTS
 */

export interface SidebarViewWrapperProps extends SidebarProps, ViewWrapperProps {
  headerContent?: React.ReactNode;
}

export const SidebarViewWrapper: React.SFC<SidebarViewWrapperProps> = (props) => {
  const {
    sidebarItems,
    sidebarPlacement,
    includeSidebarOnLgDown,
    headerContent,
    children,
    ...viewWrapperProps
  } = props;

  return (
    <ViewWrapper {...viewWrapperProps}>
      {headerContent}

      <SidebarViewBody
        sidebarItems={sidebarItems}
        sidebarPlacement={sidebarPlacement}
        includeSidebarOnLgDown={includeSidebarOnLgDown}
      >
        {children}
      </SidebarViewBody>
    </ViewWrapper>
  );
};
