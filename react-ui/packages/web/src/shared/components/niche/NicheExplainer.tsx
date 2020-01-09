import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { videoIds } from '../../constants/videoIds';
import frame from '../../../assets/frame-video.svg';
import placeholder from '../../../assets/niche-explainer-placeholder.png';
import styled from '../../styled';

// tslint:disable no-any
const FrameWrapper = styled<FlexContainerProps>(FlexContainer)`
    position: relative;
    cursor: pointer;
    margin-bottom: 100px;
    
    &:hover {
      div:last-child {
        background: rgba(93, 93, 93, 0.85);
        transition: all .15s ease-in-out;
      }
    }
    
    @media screen and (max-width: 500px) {
      max-width: 450px;
    }
    
    > img {
      width: 100%;
    }
  `;

const PlaceholderWrapper = styled.div`
    position: absolute;
    top: 30px;
    right: 30px;
    left: 30px;
    bottom: 30px;
    object-fit: cover;
    
    img {
      width: 100%;
      height: 100%;
    }
`;

const PlayBtnWrapper = styled<FlexContainerProps>(FlexContainer)`
  position: absolute;
  background: rgba(93, 93, 93, 0.65);
  height: 40px;
  width: 80px;
  padding: 25px 39px 25px 48px;
  border-radius: 8px;
  transition: all .15s ease-in-out;
  
  a {
    width: 32px;
    height: 32px;
    position: absolute;
    border-style: solid;
    border-color: transparent #fff transparent #fff;
    border-width: 16px 0 16px 32px;
    transition: all .15s ease-in-out;
    cursor: pointer;
  }
`;

interface ParentProps {
  // tslint:disable-next-line no-any
  onFrameClick: (videoId: string) => any;
}

type Props =
  ParentProps;

export const NicheExplainer: React.SFC<Props> = (props) => {
  const { onFrameClick } = props;

  return (
    <FrameWrapper centerAll={true} onClick={() => onFrameClick(videoIds.nicheExplainer)}>
      <img src={frame}/>

      <PlaceholderWrapper>
        <img src={placeholder}/>
      </PlaceholderWrapper>

      <PlayBtnWrapper centerAll={true}>
        <a/>
      </PlayBtnWrapper>
    </FrameWrapper>
  );
};
