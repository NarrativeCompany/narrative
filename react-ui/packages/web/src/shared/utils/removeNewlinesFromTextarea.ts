import * as React from 'react';

// tslint:disable-next-line no-any
export type SetFieldValue = (field: string, value: any) => void;

export function removeNewLineFromTextArea (
  target: HTMLTextAreaElement,
  fieldName: string,
  setFieldValue: SetFieldValue
) {
  setFieldValue(fieldName, target.value.replace(/\r?\n|\r/gm, ''));
}

export const textAreaNewlineRemovalProps = (fieldName: string, setFieldValue: SetFieldValue) => ({
  onKeyDown: (e: React.KeyboardEvent) => {
    if (e.keyCode === 13) {
      e.preventDefault();
    }
  },
  onPaste: (e: React.ClipboardEvent<HTMLTextAreaElement>) => {
    const target = e.currentTarget;

    if (!target) {
      return;
    }

    setTimeout(
      () => removeNewLineFromTextArea(target, fieldName, setFieldValue),
      0
    );
  }
});
