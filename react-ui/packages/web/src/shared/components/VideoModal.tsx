import * as React from 'react';
import { Modal } from 'antd';
import { ModalProps } from 'antd/lib/modal';
import YouTubePlayer from 'react-youtube';
import styled from '../styled';

const StyledVideoModal = styled<ModalProps>((props) => <Modal {...props}/>)`
  .ant-modal-body {
    padding: 0;
  }
  
  .ant-modal-close {
    background: rgba(0, 0, 0, 0.095);
    top: -50px;
    
    .ant-modal-close-x {
      font-size: 24px;
      color: #fff;
    }
  }
  
  iframe {
    margin-bottom: -5px;
  }
`;

// tslint:disable no-any
interface ParentProps {
  videoId?: string;
  visible: boolean;
  dismiss: () => any;
}
// tslint:enable no-any

export const VideoModal: React.SFC<ParentProps> = (props) => {
  const { videoId, visible, dismiss } = props;

  return (
    <StyledVideoModal
      onCancel={dismiss}
      destroyOnClose={true}
      visible={visible}
      footer={null}
      width={640}
    >
      {videoId &&
      <YouTubePlayer
        videoId={videoId}
        opts={{
          height: '390',
          width: '100%',
          playerVars: {
            autoplay: 1
          }
        }}
      />}
    </StyledVideoModal>
  );
};
