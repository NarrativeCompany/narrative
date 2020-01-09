import { css } from '../index';

export const commentBodyStyles = css`
  /* bl: for our plain-text inputs, we don't want any margin-bottom on paragraphs */
  p {
    margin-bottom: 0;
  }
  
  /* bl: let's tighten up empty paragraph spacing a bit. use padding so that it doesn't collapse like margin */
  p:empty {
    padding-bottom: 0.8em;
  }
  
  a {
    color: ${props => props.theme.primaryBlue};
    &:hover, &:active {
      color: ${props => props.theme.secondaryBlue};
    }
  }
`;
