import { ModalProps } from 'antd/lib/modal';

export const printableModalClass = 'with-printable-modal';

export const printableModalProps: Pick<ModalProps, 'afterClose'> = {
  afterClose: () => document.body.classList.remove(printableModalClass)
};
