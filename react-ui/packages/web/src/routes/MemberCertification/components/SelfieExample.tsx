import * as React from 'react';
import styled from '../../../shared/styled';
import selfie from '../../../assets/certification-selfie.svg';

const ImgWrapper = styled.div`
  width: 180px;
  
  img {
    width: 100%;
  }
  
  @media screen and (max-width: 767px) {
    display: none;
  }
`;

export const SelfieExample: React.SFC<{}> = () => {
  return (
    <ImgWrapper>
      <img src={selfie} alt=""/>
    </ImgWrapper>
  );
};
