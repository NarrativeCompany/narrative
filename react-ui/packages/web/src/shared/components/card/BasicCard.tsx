import * as React from 'react';
import { CardButtonFooter, CardProps, StyledCard } from '../Card';
import { Heading, HeadingSize } from '../Heading';
import { FlexContainer } from '../../styled/shared/containers';
import { Omit } from 'recompose';
import { Block } from '../Block';

export interface BasicCardProps extends Omit<CardProps, 'bottomMargin'> {
  title: React.ReactNode;
  afterTitle?: React.ReactNode;
  titleSize?: HeadingSize;
  titleItemProp?: string;
  descriptionItemProp?: string;
  footer?: React.ReactNode;
}

export const BasicCard: React.SFC<BasicCardProps> = (props) => {
  const {
    title,
    afterTitle,
    titleSize,
    titleItemProp,
    descriptionItemProp,
    footer,
    children,
    ...cardProps
  } = props;

  return (
    <StyledCard bottomMargin={15} {...cardProps}>
      <FlexContainer column={true}>
        <FlexContainer alignItems="flex-start" justifyContent="space-between">
          <Heading size={titleSize || 4} itemProp={titleItemProp}>{title}</Heading>
          {afterTitle}
        </FlexContainer>
        <Block color="light" itemProp={descriptionItemProp}>{children}</Block>
      </FlexContainer>

      {footer &&
        <CardButtonFooter>
          {footer}
        </CardButtonFooter>
      }
    </StyledCard>
  );
};
