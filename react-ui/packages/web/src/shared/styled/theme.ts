interface ThemeLayout {
  layoutMaxWidth: number;
  layoutBackground: {
    gray: string;
    white: string;
  };
  headerBackground: {
    gray: string;
    white: string;
  };
}

export const themeLayout: ThemeLayout = {
  layoutMaxWidth: 1120,
  layoutBackground: {
    gray: '#fcfcfd',
    white: '#fff'
  },
  headerBackground: {
    gray: '#f8f8f8',
    white: '#fff'
  }
};

interface ThemeScreenSize {
  xs: number;
  sm: number;
  md: number;
  lg: number;
  xl: number;
}

export const themeScreenSize: ThemeScreenSize = {
  xs: 575,
  sm: 767,
  md: 991,
  lg: 1199,
  xl: 1599
};

interface ThemeColors {
  primaryBlue: string;
  secondaryBlue: string;
  darkBlue: string;
  metallicBlue: string;
  lightBlue: string;
  lightOrange: string;
  primaryOrange: string;
  secondaryOrange: string;
  primaryRed: string;
  secondaryRed: string;
  primaryGreen: string;
  brightGreen: string;
  primaryPink: string;
  primaryBlack: string;
  secondaryBlack: string;
  gold: string;
  purple: string;
  pink: string;
  limeGreen: string;
  greyBlue: string;
  mediumGray: string;
  borderGrey: string;
  lightGray: string;
  bgBlack: string;
  bgBlackSecondary: string;
  layoutBg: string;
}

export const themeColors: ThemeColors = {
  primaryBlue: '#40a9ff',
  secondaryBlue: '#008afb',
  darkBlue: '#0187B2',
  metallicBlue: '#ABC1DF',
  lightBlue: '#F2FBFD',
  lightOrange: '#FFB53A',
  primaryOrange: '#FB835A',
  secondaryOrange: '#FF644A',
  primaryRed: '#FF4F4F',
  secondaryRed: '#FF6969',
  primaryGreen: '#00B38E',
  brightGreen: '#05CDA4',
  primaryPink: '#DA71EA',
  primaryBlack: '#272A2D',
  secondaryBlack: '#191B1D',
  gold: '#F5CB07',
  purple: '#DA71EA',
  pink: '#FFAEAE',
  limeGreen: '#C5D83F',
  greyBlue: '#BAC2CF',
  mediumGray: '#5d5d5d',
  borderGrey: '#e9e9e9',
  lightGray: '#bdbdbd',
  bgBlack: '#1F2F34',
  bgBlackSecondary: '#172327',
  layoutBg: '#fcfcfd',
};

export type ThemeColorType = keyof typeof themeColors;

interface ThemeTypography {
  textColor: string;
  textColorLight: string;
  textColorDark: string;
  textFontSizeExtraSmall: string;
  textFontSizeSmall: string;
  textFontSizeDefault: string;
  textFontSizeLarge: string;
  h1FontSize: number;
  h2FontSize: number;
  h3FontSize: number;
  h4FontSize: number;
  h5FontSize: number;
  h6FontSize: number;
}

export const themeTypography: ThemeTypography = {
  textColor: '#5d5d5d',
  textColorLight: '#999999',
  textColorDark: '#272A2D',
  textFontSizeExtraSmall: '10px',
  textFontSizeSmall: '12px',
  textFontSizeDefault: '14px',
  textFontSizeLarge: '16px',
  h1FontSize: 40,
  h2FontSize: 32,
  h3FontSize: 24,
  h4FontSize: 18,
  h5FontSize: 14,
  h6FontSize: 12
};

interface ThemeButtons {
  buttonBorderRadius: string;
  smallButtonPadding: string;
  defaultButtonPadding: string;
  largeButtonPadding: string;
  defaultButtonHeight: string;
  defaultButtonFontSize: string;
  smallButtonHeight: string;
  smallButtonFontSize: string;
  largeButtonHeight: string;
  largeButtonFontSize: string;
  followButtonBorderColor: string;
  followButtonActiveBg: string;
}

export const themeButtons: ThemeButtons = {
  buttonBorderRadius: '25px',
  smallButtonPadding: '0 18px',
  defaultButtonPadding: '0 24px',
  largeButtonPadding: '0 36px',
  smallButtonHeight: '24px',
  smallButtonFontSize: '10px',
  defaultButtonHeight: '32px',
  defaultButtonFontSize: '12px',
  largeButtonHeight: '40px',
  largeButtonFontSize: '14px',
  followButtonBorderColor: '#E2E6EC',
  followButtonActiveBg: '#4c4c4c'
};

interface ThemeTags {
  defaultTagColor: string;
  defaultTagBackgroundColor: string;
  defaultTagBackgroundColorHover: string;
}

export const themeTags: ThemeTags = {
  defaultTagColor: '#BAC2CF',
  defaultTagBackgroundColor: '#fff',
  defaultTagBackgroundColorHover: '#f8f8f8',
};

export type ThemeInterface =
  ThemeLayout &
  ThemeScreenSize &
  ThemeColors &
  ThemeTypography &
  ThemeButtons &
  ThemeTags;
