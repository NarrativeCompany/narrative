import * as React from 'react';
import { Button, ButtonProps } from './Button';
import { Omit } from 'recompose';
import { printableModalClass } from '../utils/modalUtils';

export const ModalPrintButton: React.SFC<Omit<ButtonProps, 'onClick' | 'href'>> = (props) => {
  return (
    <Button
      {...props}
      onClick={() => {
        if (!document.body.classList.contains(printableModalClass)) {
          document.body.classList.add(printableModalClass);
        }
        window.print();
        return false;
      }}
    />
  );
};
