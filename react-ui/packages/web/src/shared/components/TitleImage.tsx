import * as React from 'react';
import styled from 'styled-components';
import { Omit } from 'recompose';

const TitleImageBox = styled.div`
  height:auto;
  width:100%;
  margin-bottom: 20px;
`;

const TitleImageBody = styled.div<Omit<Props, 'className'>>`
  background-image: url('${p => p.imageUrl}');
  margin:0;
  background-position: center ${p => p.verticalPosition || 'center'};
  background-size: cover;
  
  &:before {
    content: '';
    height: 0;
    overflow: hidden;
    position: relative;
    display:block;
    padding-bottom: ${p => p.heightRatio || 66.66667}%;
  }
`;

interface Props {
  imageUrl: string;
  heightRatio?: number;
  // jw: this will default to top
  verticalPosition?: 'top' | 'center' | 'bottom';
  className?: string;
}

export const TitleImage: React.SFC<Props> = (props) => {
  const { className, ...titleImageProps } = props;

  return (
    <TitleImageBox className={className}>
      <TitleImageBody {...titleImageProps} />
    </TitleImageBox>
  );
};
