import * as React from 'react';
import { Heading } from './Heading';
import { Text } from './Text';
import { FlexContainer, FlexContainerProps } from '../styled/shared/containers';
import styled, { css } from '../styled';

const SectionHeaderWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 25px;
`;

const TitleWrapper = styled<
  FlexContainerProps
  & {noBottomBorder?: boolean}
  & {description?: React.ReactNode}
  >(FlexContainer)`
  
  padding-bottom: 10px;
  ${p => (!p.noBottomBorder ? `border-bottom: 1px solid ${p.theme.borderGrey};` : ``)}
`;

interface ExtraWrapperProps extends FlexContainerProps {
  size?: SectionHeaderSize;
}

const ExtraWrapper = styled<ExtraWrapperProps>(({size, ...rest}) => <FlexContainer {...rest}/>)`
  margin-left: auto;
  ${p => getLineHeight(p.size)}
`;

type SectionHeaderSize = 'sm' | 'md' | 'lg';

export interface SectionHeaderProps {
  title: React.ReactNode;
  description?: React.ReactNode;
  size?: SectionHeaderSize;
  style?: React.CSSProperties;
  id?: string;
  extra?: React.ReactNode;
  noBottomBorder?: boolean;
}

export const SectionHeader: React.SFC<SectionHeaderProps> = (props) => {
  const { id, title, size, description, noBottomBorder, style, extra } = props;

  return (
    <SectionHeaderWrapper id={id} column={true} style={style}>
      <TitleWrapper alignItems="center" noBottomBorder={noBottomBorder}>
        <Heading size={getSectionHeaderTitleSize(size)} weight={300} noMargin={true}>
          {title}
        </Heading>

        {extra &&
        <ExtraWrapper size={size} alignItems="center">
          {extra}
        </ExtraWrapper>}
      </TitleWrapper>

      {description &&
      <Text color="light" style={{marginTop: 10}}>
        {description}
      </Text>}
    </SectionHeaderWrapper>
  );
};

function getSectionHeaderTitleSize (size?: SectionHeaderSize) {
  switch (size) {
    case 'sm':
      return 5;
    case 'lg':
      return 3;
    default:
      return 4;
  }
}

// jw: it is vital that we use the same line-height as the one that will be used for the header for the 'extra' content
function getLineHeight(size?: SectionHeaderSize) {
  switch (size) {
    case 'sm':
      return css`line-height: ${p => p.theme.h5FontSize + 4}px;`;
    case 'lg':
      return css`line-height: ${p => p.theme.h3FontSize + 4}px;`;
    default:
      return css`line-height: ${p => p.theme.h4FontSize + 4}px;`;
  }
}
