import * as React from 'react';
import { Icon, Spin, Upload } from 'antd';
import { FormattedMessage } from 'react-intl';
import defaultAvatar from '../../../assets/default-avatar@1x.png';
import styled, { StyledFunction } from 'styled-components';
import { MemberProfileWrapperMessages } from '../../i18n/MemberProfileWrapperMessages';
import { FlexContainer } from '../../styled/shared/containers';
import { compose, withHandlers } from 'recompose';
import {
  buildMultipartFileArrayBodySerializerFn,
  User,
  withState,
  WithStateProps,
  withUploadCurrentUserAvatar,
  WithUploadCurrentUserAvatarProps
} from '@narrative/shared';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { themeColors } from '../../styled/theme';
import { withExtractedCurrentUser, WithExtractedCurrentUserProps } from '../../containers/withExtractedCurrentUser';
import { showValidationErrorDialogIfNecessary } from '../../utils/webErrorUtils';

interface ParentProps {
  user: User;
  size: number;
}

interface WithHandlers {
  // tslint:disable-next-line no-any
  handleFileUpload: () => any;
  // tslint:disable-next-line no-any
  beforeUpload: (file: File) => any;
}

interface State {
  file?: File;
  isUploading: boolean;
}

const initialState: State = {
  file: undefined,
  isUploading: false
};

type Props =
  ParentProps &
  WithExtractedCurrentUserProps &
  WithStateProps<State> &
  WithHandlers &
  InjectedIntlProps;

interface DivProps {
  avatarSize: number;
}

interface UploadingDivProps extends DivProps {
  isUploading: boolean;
}

const propDiv: StyledFunction<DivProps & React.HTMLProps<HTMLInputElement>> = styled.div;
const uploadingPropDiv: StyledFunction<UploadingDivProps & React.HTMLProps<HTMLInputElement>> = styled.div;
const propImg: StyledFunction<DivProps & React.HTMLProps<HTMLInputElement>> = styled.img;

const AvatarWrapper = propDiv`
  max-width: ${props => props.avatarSize + 'px'};
  margin: 0 auto;
  margin-bottom: 25px; 
`;

const AvatarContainer = propDiv`
  position: relative;
  width: ${props => props.avatarSize + 'px'};
  height: ${props => props.avatarSize + 'px'};
`;

const AvatarImage = propImg`
  position: absolute;
  width: ${props => props.avatarSize + 'px'};
  height: ${props => props.avatarSize + 'px'};
  object-fit: cover;
  border-radius: 50%;
  z-index: 0;
`;

const OverlayButton = uploadingPropDiv`
  position: absolute;
  height: ${props => (props.avatarSize / 2) + 'px'};
  width: ${props => props.avatarSize + 'px'};
  top: ${props => (props.avatarSize / 2) + 'px'};
  border-bottom-left-radius: ${props => props.avatarSize + 'px'};
  border-bottom-right-radius: ${props => props.avatarSize + 'px'};
  
  // Clip down the semicircle - args are top, right, bottom, left
  clip: rect(
      ${props => (props.avatarSize / 5) + 'px'}, 
      ${props => props.avatarSize + 'px'}, 
      ${props => (props.avatarSize / 2) + 'px'}, 
      0px);
  
  z-index: 1;
  
  // De-emphasize the ovelay button while uploading 
  background: ${props => props.isUploading ? 'rgba(0, 0, 0, 0.1)' : 'rgba(0, 0, 0, 0.3)'};
  color: ${props => props.isUploading ? 'rgba(255, 255, 255, 0.5)' : 'rgba(255, 255, 255, 0.75)'};;
  
  ${props => !props.isUploading &&  `
    transition: 0.3s;
    
    &:hover {
     background: rgba(0, 0, 0, 0.6);
     color: white;
     cursor: pointer;
    }
  `}
`;

const IconContainer = uploadingPropDiv`
  position: relative;
  margin-top: ${props => (props.avatarSize / 4) + 'px'}
  display: block;
  margin-left: auto;
  margin-right: auto;
  width: 100%;
`;

const ProgressContainer = propDiv`
  position: absolute;
  margin-top: ${props => (props.avatarSize / 4) + 'px'}
  margin-left: ${props => (props.avatarSize / 4) + 'px'}
}
`;

const AvatarComponent: React.SFC<Props> = (props) => {
  const { user, state, size, handleFileUpload, beforeUpload, currentUser } = props;
  const { isUploading } = state;

  const isCurrentUser =
    currentUser &&
    user &&
    currentUser.oid === user.oid;

  return (
    <AvatarWrapper avatarSize={size}>

      <AvatarContainer avatarSize={size}>

        <AvatarImage
          src={user.avatarSquareUrl || defaultAvatar}
          alt={user.displayName}
          avatarSize={size}
        />

        {isUploading &&
          <ProgressContainer avatarSize={size}>
             <Spin
               indicator={
                 <Icon type="loading" style={{ fontSize: size / 2, color: themeColors.primaryBlue }} spin={true}/>
               }
             />
          </ProgressContainer>
        }

        {isCurrentUser &&
          <Upload
            accept="image/*"
            beforeUpload={beforeUpload}
            customRequest={handleFileUpload}
            disabled={isUploading}
            showUploadList={false}
          >
            <OverlayButton avatarSize={size} isUploading={isUploading}>
              <IconContainer avatarSize={size} isUploading={isUploading}>
                <FlexContainer centerAll={true} >
                    <Icon type="form"/>
                    <FormattedMessage {...MemberProfileWrapperMessages.EditAvatarImage}/>
                </FlexContainer>
              </IconContainer>
            </OverlayButton>
          </Upload>
        }

      </AvatarContainer>

    </AvatarWrapper>
  );
};

export const Avatar = compose(
  injectIntl,
  withState<State>(initialState),
  withUploadCurrentUserAvatar,
  withExtractedCurrentUser,
  withHandlers({
    beforeUpload: (props: Props) => async (file: File) => {
      props.setState(ss => ({...ss, file, isUploading: true}));
      return false;
    },
    handleFileUpload: (props: Props & WithUploadCurrentUserAvatarProps) => () => {
      const { uploadCurrentUserAvatar, intl, state } = props;
      const { file } = state;
      if (file) {
        const input = {name: file.name, type: file.type};
        // Create a curried body serializer with the file array since we can't pass File or Blob via GraphQL input
        const bodySerializer = buildMultipartFileArrayBodySerializerFn([file]);
        // Indicate we are uploading
        props.setState(ss => ({...ss, isUploading: true}));

        // Wait for the upload - can't use async handler/await due to Ant Design file upload component issue
        // https://github.com/ant-design/ant-design/issues/10122
        uploadCurrentUserAvatar({input, bodySerializer})
        .then(() => {
          // Done
          props.setState(ss => ({...ss, isUploading: false}));
        })
        .catch((exception) => {
          showValidationErrorDialogIfNecessary(intl.formatMessage(SharedComponentMessages.FormErrorTitle), exception);
          props.setState(ss => ({...ss, isUploading: false}));
        });
      } else {
        // tslint:disable-next-line no-console
        console.error('No file found for upload');
      }
    }
  }),
)(AvatarComponent) as React.ComponentClass<ParentProps>;
