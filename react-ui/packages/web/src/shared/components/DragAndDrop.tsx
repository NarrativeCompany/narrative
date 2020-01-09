import * as React from 'react';
import { Upload } from 'antd';
import { UploadProps } from 'antd/lib/upload';
import { AntFormItemProps } from './FormField';
import { Field, FieldProps, FieldAttributes } from 'formik';
import { FormattedMessage } from 'react-intl';
import { FormControl } from './FormControl';
import { FlexContainer } from '../styled/shared/containers';
import { CustomIcon, IconType } from './CustomIcon';
import { Heading } from './Heading';
import { Paragraph } from './Paragraph';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import styled from '../styled';

const UploadButton = styled<UploadProps>(Upload.Dragger)`
  width: 100%;

  .ant-upload-drag {
    border: none !important;
    height: auto !important;
  }
  
  .ant-upload-btn {
    padding: 0 !important;
    height: auto !important;
  }

  .ant-upload-drag-container {
    display: flex !important;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: ${p => p.theme.layoutBg};
    border: 1px dashed ${p => p.theme.primaryBlue};
    border-radius: 4px;
    padding: 35px 70px;
    cursor: pointer;
    height: 100%;
    
    img {
      margin-bottom: 15px;
    }
  }
`;

interface ParentProps {
  dndTitle: React.ReactNode;
  iconType: IconType;
  wrapperStyle?: React.CSSProperties;
}

export type ImageUploadProps =
  UploadProps &
  ParentProps;

export const DragAndDrop: React.SFC<ImageUploadProps> = (props) => {
  const { dndTitle, iconType, wrapperStyle, ...uploadProps } = props;

  return (
    <FlexContainer style={wrapperStyle}>
      <UploadButton {...uploadProps}>
        <CustomIcon type={iconType} size={50}/>

        <Heading size={5}>
          {dndTitle}
        </Heading>

        <Paragraph color="primary">
          <FormattedMessage {...SharedComponentMessages.ClickAndDrag}/>
        </Paragraph>
      </UploadButton>
    </FlexContainer>
  );
};

// this component is for use in formik forms (allows you to add name and values attributes and handles field errors)
type DragAndDropFieldProps =
  FieldAttributes<{}> &
  ImageUploadProps &
  AntFormItemProps;

export const DragAndDropField: React.SFC<DragAndDropFieldProps> = (props) => {
  const {
    name,
    formFieldStyle,
    label,
    labelCol,
    wrapperCol,
    extra,
    hasFeedback,
    iconType,
    wrapperStyle,
    onChange,
    dndTitle,
    ...inputProps
  } = props;

  const renderInput = (fieldProps: FieldProps<{}>) => {
    const { field, form: { touched, errors } } = fieldProps;
    const errorMessage = touched[field.name] && errors[field.name];
    const extractedFieldProps = { name: field.name, value: field.value, onBlur: field.onBlur };

    return (
      <FormControl
        style={formFieldStyle}
        label={label}
        labelCol={labelCol}
        wrapperCol={wrapperCol}
        help={errorMessage}
        validateStatus={errorMessage ? 'error' : undefined}
        hasFeedback={hasFeedback || false}
        extra={extra}
      >
        <DragAndDrop
          dndTitle={dndTitle}
          iconType={iconType}
          wrapperStyle={wrapperStyle}
          onChange={onChange}
          {...extractedFieldProps}
          {...inputProps}
        />
      </FormControl>
    );
  };

  return (
    <Field
      name={name}
      render={renderInput}
    />
  );
};
