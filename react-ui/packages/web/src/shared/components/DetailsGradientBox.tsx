import styled, { css } from '../styled';

const backgroundColor = {
  blue: css`
    background: linear-gradient(${p => p.theme.primaryBlue}, ${p => p.theme.secondaryBlue});  
  `,
  darkBlue: css`
    background: ${p => p.theme.darkBlue}
  `,
  black: css`
    background: linear-gradient(${p => p.theme.primaryBlack}, #555);
  `
};

export type DetailsGradientBoxColor = keyof typeof backgroundColor;
export type DetailsGradientBoxSize = 'default' | 'large';

interface DetailsGradientBoxProps {
  color?: DetailsGradientBoxColor;
  size?: DetailsGradientBoxSize;
}

export const DetailsGradientBox = styled<DetailsGradientBoxProps, 'div'>('div')`
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  width: 100%;
  height: ${p => p.size && p.size === 'large' ? '450px' : '350px'}
  ${p => p.color ? backgroundColor[p.color] : backgroundColor.blue};
`;
