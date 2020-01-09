import * as React from 'react';
import styled from '../styled';
import { getTextColor, getTextSize, getTextTransform, WithTextColor, WithTextSize, WithTextTransform } from './Text';
import { HTMLAttributes } from 'react';

export type BlockProps =
  WithTextColor & WithTextTransform & WithTextSize & Pick<HTMLAttributes<{}>, 'style' | 'className' | 'itemProp'>;

export const Block =
  styled<BlockProps>((
    {color, transform, size, ...rest}
  ) => <div {...rest}>{rest.children}</div>)`
    ${props => getTextColor(props)};
    ${props => getTextTransform(props)};
    ${props => getTextSize(props)};
  `;
