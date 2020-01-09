import * as React from 'react';
import { compose } from 'recompose';
import { AboutSection } from '../components/AboutSection';
import { NicheExplainerMessages } from '../../../shared/i18n/NicheExplainerMessages';
import { NicheExplainer } from '../../../shared/components/niche/NicheExplainer';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { VideoModal } from '../../../shared/components/VideoModal';
import { withState, WithStateProps } from '@narrative/shared';
import { videoIds } from '../../../shared/constants/videoIds';
import styled from '../../../shared/styled';
import { AboutSectionParagraph } from '../components/AboutSectionParagraph';

const FrameWrapper = styled<FlexContainerProps>(FlexContainer)`
  max-width: 450px;
  margin: 40px auto 0;
`;

interface State {
  isVideoModalVisible: boolean;
}
const initialState: State = {
  isVideoModalVisible: false,
};

const AboutNichesSectionComponent: React.SFC<WithStateProps<State>> = (props) => {
  const { state, setState } = props;

  return (
    <AboutSection
      title={NicheExplainerMessages.AboutNichesSectionTitle}
      titleType="niche"
      style={{ marginBottom: 0 }}
    >
      <AboutSectionParagraph message={NicheExplainerMessages.AboutNichesSectionParagraphOne}/>

      <FrameWrapper centerAll={true}>
        <NicheExplainer onFrameClick={() => setState(ss => ({ ...ss, isVideoModalVisible: true }))}/>
      </FrameWrapper>

      <VideoModal
        videoId={videoIds.nicheExplainer}
        visible={state.isVideoModalVisible}
        dismiss={() => setState(ss => ({ ...ss, isVideoModalVisible: false }))}
      />
    </AboutSection>
  );
};

export const AboutNichesSection = compose(
  withState<State>(initialState)
)(AboutNichesSectionComponent) as React.ComponentClass<{}>;
