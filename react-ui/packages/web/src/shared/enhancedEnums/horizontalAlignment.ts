import { HorizontalAlignment } from '@narrative/shared';
import { EnumEnhancer } from '../utils/enhancedEnumUtils';
import { FormattedMessage } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import { CSSProperties } from 'react';

// jw: let's define the HorizontalAlignmentHelper that will provide all the extra helper logic for
//     HorizontalAlignments
export class HorizontalAlignmentHelper {
  alignment: HorizontalAlignment;
  title: FormattedMessage.MessageDescriptor;
  css: CSSProperties;

  constructor(
    alignment: HorizontalAlignment,
    title: FormattedMessage.MessageDescriptor,
    css: CSSProperties
  ) {
    this.alignment = alignment;
    this.title = title;
    this.css = css;
  }
}

// jw: next: lets create the lookup of HorizontalAlignment to helper object

const alignmentHelpers: {[key: number]: HorizontalAlignmentHelper} = [];
// jw: make sure to register these in the order you want them to display.
alignmentHelpers[HorizontalAlignment.LEFT] = new HorizontalAlignmentHelper(
  HorizontalAlignment.LEFT,
  SharedComponentMessages.HorizontalAlignmentLeft,
  {textAlign: 'left'}
);
alignmentHelpers[HorizontalAlignment.CENTER] = new HorizontalAlignmentHelper(
  HorizontalAlignment.CENTER,
  SharedComponentMessages.HorizontalAlignmentCenter,
  {textAlign: 'center'}
);
alignmentHelpers[HorizontalAlignment.RIGHT] = new HorizontalAlignmentHelper(
  HorizontalAlignment.RIGHT,
  SharedComponentMessages.HorizontalAlignmentRight,
  {textAlign: 'right'}
);

// jw: finally, let's create the enhancer, which will allow us to lookup helpers by enum instance.
export const EnhancedHorizontalAlignment = new EnumEnhancer<HorizontalAlignment, HorizontalAlignmentHelper>(
  alignmentHelpers
);
