import { ThemeInterface, themeScreenSize } from '../theme';
import { InterpolationValue, ThemedCssFunction } from 'styled-components';
import { css } from '../index';

export const ScreenSizeNames = Object.keys(themeScreenSize) as Array<keyof typeof themeScreenSize>;

// jw: unfortunately, we need to define these manually to make sure the types are clean!
interface MediaQueries {
  // jw: all of the type specific queries. These will only apply to that specific size.
  xs: ThemedCssFunction<ThemeInterface>;
  sm: ThemedCssFunction<ThemeInterface>;
  md: ThemedCssFunction<ThemeInterface>;
  lg: ThemedCssFunction<ThemeInterface>;
  xl: ThemedCssFunction<ThemeInterface>;

  // jw: All the queries for the size and up
  sm_up: ThemedCssFunction<ThemeInterface>;
  md_up: ThemedCssFunction<ThemeInterface>;
  lg_up: ThemedCssFunction<ThemeInterface>;

  // jw: All the queries for the size and down
  sm_down: ThemedCssFunction<ThemeInterface>;
  md_down: ThemedCssFunction<ThemeInterface>;
  lg_down: ThemedCssFunction<ThemeInterface>;

  // jw: These interpolations will hide at the specified size
  hide_xs: InterpolationValue[];
  hide_sm: InterpolationValue[];
  hide_md: InterpolationValue[];
  hide_lg: InterpolationValue[];
  hide_xl: InterpolationValue[];

  // jw: These interpolations will hide anything for the size and up
  hide_sm_up: InterpolationValue[];
  hide_md_up: InterpolationValue[];
  hide_lg_up: InterpolationValue[];

  // jw: These interpolations will hide anything for the size and down
  hide_sm_down: InterpolationValue[];
  hide_md_down: InterpolationValue[];
  hide_lg_down: InterpolationValue[];
}

export const mediaQuery = (ScreenSizeNames).reduce((acc, sizeName) => {
  const index = ScreenSizeNames.indexOf(sizeName);
  const upperScreenSize = themeScreenSize[sizeName];
  const isFirst = index === 0;
  const isLast = index === ScreenSizeNames.length - 1;

  // tslint:disable no-any
  // jw: if this is the first item (sm) there is not a lot to define. 
  if (isFirst) {
    acc[sizeName] = (first: any, ...interpolations: any[]) => css`
      @media screen and (max-width: ${upperScreenSize}px) {
        ${css(first, ...interpolations)}
      }
    `;
    acc[`hide_${sizeName}`] = css`
      @media screen and (max-width: ${upperScreenSize}px) {
        display: none;
      }
    `;

  // jw: if this is anything above the first, then things get a bit trickier.
  } else {
    const previousSizeName: string = ScreenSizeNames[index - 1];
    const lowerScreenSize = themeScreenSize[previousSizeName] + 1;

    // jw: if this is the last item, then things are pretty simple again.
    if (isLast) {
      acc[sizeName] = (first: any, ...interpolations: any[]) => css`
        @media screen and (min-width: ${lowerScreenSize}px) {
          ${css(first, ...interpolations)}
        }
      `;
      acc[`hide_${sizeName}`] = css`
        @media screen and (min-width: ${lowerScreenSize}px) {
          display: none;
        }
      `;

    // jw: at this point we know we are somewhere in the middle, so we need to define a couple more media selectors
    } else {
      acc[sizeName] = (first: any, ...interpolations: any[]) => css`
        @media screen and (min-width: ${lowerScreenSize}px) and (max-width: ${upperScreenSize}px) {
          ${css(first, ...interpolations)}
        }
      `;

      acc[`${sizeName}_up`] = (first: any, ...interpolations: any[]) => css`
        @media screen and (min-width: ${lowerScreenSize}px) {
          ${css(first, ...interpolations)}
        }
      `;

      acc[`${sizeName}_down`] = (first: any, ...interpolations: any[]) => css`
        @media screen and (max-width: ${upperScreenSize}px) {
          ${css(first, ...interpolations)}
        }
      `;

      acc[`hide_${sizeName}`] = css`
        @media screen and (min-width: ${lowerScreenSize}px) and (max-width: ${upperScreenSize}px) {
          display: none;
        }
      `;

      // jw: we know that we can hide content relative to this viewport
      acc[`hide_${sizeName}_up`] = css`
        @media screen and (min-width: ${lowerScreenSize}px) {
          display: none;
        }
      `;

      acc[`hide_${sizeName}_down`] = css`
        @media screen and (max-width: ${upperScreenSize}px) {
          display: none;
        }
      `;
    }
  }  
  // tslint:enable no-any

  return acc;
}, {} as MediaQueries);
