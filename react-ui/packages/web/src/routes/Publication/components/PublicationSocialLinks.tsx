import * as React from 'react';
import styled, { css } from '../../../shared/styled';
import { mediaQuery } from '../../../shared/styled/utils/mediaQuery';

interface StyleProps {
  forHeader?: boolean;
}

interface Props extends StyleProps {
  links?: React.ReactNode[];
}

export const publicationIconStyle = css`
  .anticon {
    font-size: 16px;
  }
`;

const Container = styled.div<StyleProps>`
  ${p => p.forHeader 
    ? css`
      ${mediaQuery.hide_sm_down};
      display: inline-block;
    `
    : css`
      ${mediaQuery.hide_md_up};
      margin-top: 50px;
      text-align: center;
    `
}
  
  > *:not(:first-child) {
    margin-left: 10px;
  }
  
  ${publicationIconStyle}
`;

export const PublicationSocialLinks: React.SFC<Props> = (props) => {
  const { links, ...styleProps } = props;

  if (!links || !links.length) {
    return null;
  }

  return (
    <Container {...styleProps}>
      {links.map((link, i) => <React.Fragment key={i}>{link}</React.Fragment>)}
    </Container>
  );
};
