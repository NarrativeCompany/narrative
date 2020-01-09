import { css } from '../index';

/**
 * style overrides for the froala editor inline toolbar
 * these need to be injected globally as the inline toolbar sits outside of the editor wrapper
 */
export const froalaInlineToolbar = css`
  .fr-popup h3 {
    color: #fff;
  }
  
  .fr-placeholder {
    color: rgb(179, 179, 177) !important;
  }
  
  .fr-toolbar {
    border-top: 0 !important;
    border-bottom: 0 !important;
    z-index: 1000;
  }
  
  .fr-toolbar.fr-inline .fr-arrow {
    top: -5px !important;
  }
  
  .fr-toolbar.fr-inline.fr-above .fr-arrow {
    top: auto !important;
    bottom: -5px !important;
    border-top-color: #222 !important;
  }
  
  // make sure all dropdown list items or text in general is white
  .fr-command.fr-btn+.fr-dropdown-menu .fr-dropdown-wrapper .fr-dropdown-content ul.fr-dropdown-list li a {
    color: #fff !important;
  }
  
  .fr-command.fr-btn+.fr-dropdown-menu .fr-dropdown-wrapper .fr-dropdown-content ul.fr-dropdown-list li h1 {
    font-size: 16px !important;
  }
  
  .fr-command.fr-btn+.fr-dropdown-menu .fr-dropdown-wrapper .fr-dropdown-content ul.fr-dropdown-list li h2 {
    font-size: 14px !important;
  }
`;
