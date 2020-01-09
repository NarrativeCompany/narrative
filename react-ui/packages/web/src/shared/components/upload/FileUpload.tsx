import * as React from 'react';
import { compose, Omit } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { Upload } from 'antd';
import { UploadChangeParam, UploadProps } from 'antd/lib/upload';
import { FileUploadInput, withState, WithStateProps } from '@narrative/shared';
import { FileUploadMessages } from '../../i18n/FileUploadMessages';
import { FileUsageType } from '../../utils/fileUploadUtils';
import { Heading, HeadingProps } from '../Heading';
import { Paragraph, ParagraphProps } from '../Paragraph';
import styled, { css } from '../../styled';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { UploadFile } from 'antd/lib/upload/interface';

export interface FileUploadState {
  inError?: boolean;
  input?: FileUploadInput;
}

export function setFileOnFieldValueHelper(
  // tslint:disable-next-line no-any
  setFieldValue: (field: string, value: any) => void,
  field: string
): (state: FileUploadState) => void {
  return (uploadState: FileUploadState) => {
    if (uploadState.inError) {
      // bl: the image object must always be set, so formik validation will fail if it's null
      setFieldValue(field, null);
    } else {
      setFieldValue(field, uploadState.input);
    }
  };
}

export function getExistingUploadFile(
  name: string,
  imageUrl?: string | null
): UploadFile[] | undefined {
  if (imageUrl) {
    return [{
        uid: '1',
        name,
        status: 'done',
        url: imageUrl,
        thumbUrl: imageUrl,
        size: 0,
        type: 'image'
      }];
  }

  return undefined;
}

interface State {
  showUploadLink: boolean;
  errorMessage?: string;
  file?: FileUploadInput;
  // jw: now that we are providing previously uploaded files through the properties the AntD `Upload` component requires
  //     that we manage the fileList in our own state, instead of managing that itself. As a result, we need to store
  //     the fileList here, and then update it when they give us a new list through the onUploadChange callback.
  fileList?: UploadFile[];
}

interface WrapperStyleProps {
  useImagePreview?: boolean;
}

export interface FileUploadProps extends
  Omit<UploadProps, 'action' | 'onChange' | 'onRemove' | 'listType' | 'disabled'>,
  WrapperStyleProps
{
  title?: FormattedMessage.MessageDescriptor;
  description?: FormattedMessage.MessageDescriptor;
  fileUsageType: FileUsageType;
  onChange: (state: FileUploadState) => void;
}

type Props = WithStateProps<State> & FileUploadProps;

const UploadWrapper = styled<FlexContainerProps & WrapperStyleProps>(
  ({useImagePreview, ...rest}) => <FlexContainer {...rest} />
)`
  flex-direction: column;
  margin-bottom: 24px;
  ${p => p.useImagePreview && css`
    .ant-upload-list-picture {
      /* 
        jw: allow the item to be taller than the 66px default, and ensure there is enough space on the right to show
            the close icon.
      */
      .ant-upload-list-item {
        height: auto;
        min-height: 66px;
        padding-right: 30px;
      }

      /* jw: no longer float the image! */
      .ant-upload-list-item-thumbnail {
        position: relative;
        top: 0;
        left: 0;
        width: auto;
        height: auto;
        opacity: 1;

        /* 
          jw: ensure the image consumes the full width (the padding above will ensure we keep enough room for the close
              icon, and ensure that the aspect ratio is maintained.
        */
        img {
          width: auto;
          max-width: 100%;
          height: auto;
        }
      }
      
      /* jw: suppress the file title in this case. */
      .ant-upload-list-item-name {
        display: none;
      }
      
      /* bl: hide the progress bar in this mode since it otherwise can overlap the image and look odd */
      .ant-upload-list-item-progress {
        display: none;
      }
    }
  `};
`;

const LabelWrapper = styled<FlexContainerProps>(FlexContainer)`
  flex-direction: column;
  margin-bottom: 10px;
`;

const LabelHeading = styled<HeadingProps>(Heading)`
  margin-bottom: 0;
`;

const ErrorWrapper = styled<ParagraphProps>(Paragraph)`
  color: #f5222d;
`;

const FileUploadComponent: React.SFC<Props> = (props) => {
  const {
    title,
    description,
    fileUsageType,
    onChange,
    state,
    setState,
    useImagePreview,
    ...uploadProps
  } = props;

  const onUploadChange = (info: UploadChangeParam) => {
    const { fileList } = info;
    const showUploadLink = fileList.length === 0;
    // jw: we need to persist the latest version of the fileList into the state so that it will be rendered correctly
    setState(ss => ({...ss, showUploadLink, fileList}));
    if (!showUploadLink) {
      const file = fileList[0];
      const response = file.response;
      if (file.status === 'error') {
        setState(ss => ({...ss, errorMessage: response.message}));
        onChange({inError: true});
      } else if (file.status === 'done') {
        const input = {
          tempFile: {
            oid: response.oid,
            token: response.token
          }
        };
        setState(ss => ({...ss, file: input}));
        onChange({input});
      }
    }
  };

  const onUploadRemove = () => {
    setState(ss => ({...ss, errorMessage: undefined}));
    const { file } = state;
    const input = {
      ...file,
      remove: true
    };
    onChange({input});
    return true;
  };

  const action = '/api/temp-files?fileUsageType=' + fileUsageType;
  const { errorMessage } = state;

  return (
    <UploadWrapper useImagePreview={useImagePreview}>
      <LabelWrapper>
        {title &&
          <LabelHeading size={4}>
            <FormattedMessage {...title}/>
          </LabelHeading>
        }
        {description &&
          <Paragraph color="light">
            <FormattedMessage {...description}/>
          </Paragraph>
        }
      </LabelWrapper>
      <Upload
        action={action}
        listType="picture"
        onChange={onUploadChange}
        onRemove={onUploadRemove}
        {...uploadProps}
        fileList={state.fileList}
      >
        {state.showUploadLink &&
          <a style={{fontSize: 16}}><FormattedMessage {...FileUploadMessages.ClickToUpload}/></a>
        }
      </Upload>
      {errorMessage &&
      <ErrorWrapper>
        {errorMessage}
      </ErrorWrapper>}
    </UploadWrapper>
  );
};

export const FileUpload = compose(
  withState<State, FileUploadProps>((props) => {
    const { fileList } = props;

    return {
      showUploadLink: !fileList || !fileList.length,
      fileList
    };
  })
)(FileUploadComponent) as React.ComponentClass<FileUploadProps>;
