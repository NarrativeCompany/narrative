import * as React from 'react';
import { Heading } from '../../../../shared/components/Heading';
import { HighlightedCard, HighlightColor } from '../../../../shared/components/HighlightedCard';
import { Paragraph } from '../../../../shared/components/Paragraph';

interface Props {
  stat: React.ReactNode;
  description: React.ReactNode;
  style?: React.CSSProperties;
  highlightColor: HighlightColor;
}

export const StatsCard: React.SFC<Props> = (props) => {
  const { stat, description, style, ...highlightCardProps } = props;

  return (
    <HighlightedCard
      highlightSide="top"
      highlightWidth="wide"
      style={{marginBottom: 15, ...style}}
      {...highlightCardProps}
    >
      <Heading size={2} style={{textAlign: 'center'}}>
        {stat}
      </Heading>
      <Paragraph color="light" style={{textAlign: 'center'}}>
        {description}
      </Paragraph>
    </HighlightedCard>
  );
};
