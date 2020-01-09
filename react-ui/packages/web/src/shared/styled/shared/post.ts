import styled, { css } from '../index';
import blockquote from '../../../assets/blockquote.svg';
import { mediaQuery } from '../utils/mediaQuery';
import { themeTypography } from '../theme';
import { headingLineHeightBuffer } from '../../components/Heading';

/**
 * PostWrapper definition
 * wrap the post body in the editor and in post details with this styled component
 * This isolates styles for posts to a single styled component
 */
export const PostWrapper = styled.div`
  p, span, strong, em, li {
    font-size: 18px;
    color: #344D59;
  }
  
  h1 {
    font-size: 30px;
    display: inline-block;
    margin-bottom: 10px;
    color: #192A2E;
  }
  
  h2 {
    font-size: 24px;
    display: inline-block;
    margin-bottom: 8px;
    color: #192A2E;
  }
  
  blockquote:before {
    content: url(${blockquote});
    height: 25px;
    margin-right: 15px;
    margin-top: 2.5px;
  }
  
  blockquote {
    display: flex;
    align-items: flex-start;
    border-left: none !important;
    margin: 35px 0;
    margin-left: 35px !important;
  }
  
  blockquote p {
    display: inline-block;
    font-family: 'Quicksand', 'Lato', sans-serif;
    font-size: 21px;
    color: #5d5d5d;
    margin: 0;
  }
  
  a, a em, a strong {
    color: #40a9ff !important;
  }
  
  div.imgWrapper {
    text-align:center;
    margin: 5px;
  }
  
  hr {
    border: 0;
    height: 1px;
    background-image: -webkit-linear-gradient(left, #F9FAFB, #E2E6EC, #F9FAFB);
    background-image: -moz-linear-gradient(left, #F9FAFB, #E2E6EC, #F9FAFB);
    background-image: -ms-linear-gradient(left, #F9FAFB, #E2E6EC, #F9FAFB);
    background-image: -o-linear-gradient(left, #F9FAFB, #E2E6EC, #F9FAFB);
    margin: 20px 0;
  }

  img.fr-dib {
    height: auto;
  }
  
  // zb: post caption color override
  .fr-img-caption span.fr-inner {
    color: #7C8C90 !important;
    font-size: 14px !important;
  }
  
`;

/**
 * styled component definition for our desktop editor
 * inherits from the styled post wrapper
 */
export const DesktopEditorWrapper = styled(PostWrapper)`
  ${mediaQuery.xs`display: none;`}
  
  .fr-quick-insert {
   svg {
    fill: #fff !important;
   }
  }
`;

/**
 * styled component definition for or mobile editor
 * inherits from the styled post wrapper
 */
export const MobileEditorWrapper = styled(PostWrapper)`
  display: none;
  margin-bottom: 25px;
  
  .fr-wrapper {
    box-shadow: none !important;
  }

  .fr-view {
    padding: 0 !important;
  }
  
  .dark-theme.fr-toolbar .fr-command.fr-btn, 
  .dark-theme.fr-popup .fr-command.fr-btn {
    width: 35px;
    
    i {
      font-size: 12px;
      width: 12px;
    }
  }
  
  ${mediaQuery.xs`display: block;`}
  
  // bl: hide the OL and UL buttons at these narrower resolutions to ensure the toolbar fits on a single line
  // for most mobile devices. hide the OL first, then hide the UL at even narrow resolutions.
  @media screen and (max-width: 435px) {
    button[data-cmd="formatOL"] {
      display: none;
    }
  }
  @media screen and (max-width: 395px) {
    button[data-cmd="formatUL"] {
      display: none;
    }
  }
`;

/**
 * styles for our post title and subtitle as they are separate from the post body
 */

export const postTitleBaseStyles = css`
  color: #112E34;
  font-weight: bold;
`;

export const postTitleStyles = css`
  ${postTitleBaseStyles};
  font-size: ${themeTypography.h1FontSize}px;
  line-height: ${themeTypography.h1FontSize + headingLineHeightBuffer}px;
`;

export const postSubtitleBaseStyles = css`
  color: #7C8C90;
`;

export const postSubtitleStyles = css`
  ${postSubtitleBaseStyles};
  font-size: 21px;
  line-height: 24px;
`;

/**
 * styles shared by the post create form textareas
 */
export const postInputStyles = css`
  &.ant-input::placeholder {
    color: rgb(179, 179, 177);
  }

  &.ant-input {
    border: none;
    margin-bottom: 0;
    resize: none;
    
    &:focus,
    &:active {
      border-color: transparent !important;
      box-shadow: none !important;
      border-right-width: 0 !important;
    }
  }
`;
