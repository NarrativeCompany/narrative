import * as React from 'react';
import { compose } from 'recompose';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import { Editor } from '../../../shared/components/Editor';
import { FormControl } from '../../../shared/components/FormControl';
import styled from '../../../shared/styled';

const ErrorMessage = styled.div`
  color: #f5222d;
`;

interface ParentProps {
  initialValue?: string | null;
  postOid: string;
  onChange: (model: string) => void;
  onImageUpload: (isLoading: boolean, isError?: boolean) => void;
  error?: string;
}

type Props =
  ParentProps &
  InjectedIntlProps;

export const CreatePostEditorComponent: React.SFC<Props> = (props) => {
  const { initialValue, postOid, onChange, onImageUpload, error } = props;

  return (
    <React.Fragment>
      <FormControl style={{ marginBottom: 0 }}/>

      <Editor
        initialValue={initialValue}
        postOid={postOid}
        onChange={model => onChange(model)}
        onImageUpload={onImageUpload}
      />

      {error && <ErrorMessage>{error}</ErrorMessage>}
    </React.Fragment>
  );
};

export const CreatePostEditor = compose(
  injectIntl,
)(CreatePostEditorComponent) as React.ComponentClass<ParentProps>;
