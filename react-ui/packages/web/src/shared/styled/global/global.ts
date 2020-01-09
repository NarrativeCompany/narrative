import { injectGlobal } from '../index';
import { froalaInlineToolbar } from './froala-inline-toolbar';

/**
 * Global styles definition
 * any styles that need to be applied globally need to be added here
 * function is called in web/src/index.tsx
 */

export const injectGlobalStyles = () => injectGlobal`
  body {
    margin: 0;
    padding: 0;
  }
  
  html {
    height: 100%;
    overflow: auto;
  }
  
  body {
    height: auto;
  }
  
  #root {
    height: 100%;
  }
  
  * {
    font-family: 'Lato', sans-serif;
  }
  
  ${froalaInlineToolbar};
  
  @media print {
    body.with-printable-modal {
      height: 1vh;
      
      .ant-modal-mask {
        background-color: white;
      }
    }
  }
`;
