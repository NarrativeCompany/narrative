import * as React from 'react';
import { compose } from 'recompose';
import { Row, Col, Alert } from 'antd';
import { TextAreaProps } from 'antd/lib/input';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import { StyledViewWrapper } from '../Post';
import { FormField } from '../../../shared/components/FormField';
import { CreatePostEditor } from './CreatePostEditor';
import { Button, ButtonProps } from '../../../shared/components/Button';
import { ColProps } from 'antd/lib/col';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import { PostMessages } from '../../../shared/i18n/PostMessages';
import { MAX_CANONICAL_URL_LENGTH, PostInput } from '@narrative/shared';
import { SetFieldValue, textAreaNewlineRemovalProps } from '../../../shared/utils/removeNewlinesFromTextarea';
import { postTitleStyles, postSubtitleStyles, postInputStyles } from '../../../shared/styled/shared/post';
import styled from '../../../shared/styled';
import { MAX_POST_TITLE_LENGTH } from '../../../shared/constants/constants';
import { InputField } from '../../../shared/components/InputField';
import { Link } from '../../../shared/components/Link';
import { externalUrls } from '../../../shared/constants/externalUrls';

const { TextArea, Checkbox } = FormField;

const TitleInput = styled<TextAreaProps & {hasFeedback?: boolean}>(TextArea)`  
  ${postInputStyles};

  &.ant-input {
    ${postTitleStyles};
    padding: 4px 0;
  }
`;

const SubTitleInput = styled<TextAreaProps>(TextArea)`  
  ${postInputStyles};
  
  &.ant-input {
    ${postSubtitleStyles};
    padding: 2px 0;  
  }
`;

const LeftCol = styled<ColProps>((props) => <Col {...props}/>)`
  @media screen and (max-width: 1410px) {
    padding-left: 45px !important;
  }

  @media screen and (max-width: 575px) {
    padding-left: 12px !important;
  }
`;

const RightCol = styled<ColProps>((props) => <Col {...props}/>)`
  padding-left: 40px !important;
  
  @media screen and (max-width: 1524px) {
    padding-left: 40px !important;
  }
  
  @media screen and (max-width: 1199px) {
    padding-left: 24px !important;
  }
  
  @media screen and (max-width: 991px) {
    padding-left: 24px !important;
  }

  @media screen and (max-width: 575px) {
    padding-left: 12px !important;
  }
`;

const btnProps: ButtonProps = {
  size: 'large',
  style: { width: '100%' }
};

interface ParentProps {
  onNextClick: () => void;
  onEditorChange: (model: string) => void;
  onImageUpload: (isLoading: boolean, isError?: boolean) => void;
  formValues: PostInput;
  bodyError?: string;
  postOid: string;
  isValidating: boolean;
  setFieldValue: SetFieldValue;
  postLive?: boolean;
}

type Props =
  ParentProps &
  InjectedIntlProps;

export const CreatePostFormComponent: React.SFC<Props> = (props) => {
  const {
    onNextClick,
    onEditorChange,
    onImageUpload,
    formValues,
    bodyError,
    postOid,
    isValidating,
    setFieldValue,
    postLive,
    intl,
    intl: { formatMessage }
  } = props;

  const learnMore = (
    <Link.Anchor href={externalUrls.narrativeWhatIsCanonicalLink} target="_blank">
      <FormattedMessage {...PostMessages.LearnMore}/>
    </Link.Anchor>
  );

  return (
    <StyledViewWrapper style={{ paddingLeft: 0 }}>
      <Row style={{ width: '100%', margin: '0 auto' }}>

        <LeftCol xl={17} lg={15} md={24}>
          {!formValues.draft &&
          <Alert
            type="info"
            message={<FormattedMessage {...(postLive
              ? PostMessages.LivePostAlertMessage
              : PostMessages.AutoSaveDisabledAlertMessage)}/>
            }
            showIcon={true}
            closable={true}
            style={{ marginBottom: 20 }}
          />}

          <TitleInput
            name="title"
            placeholder={formatMessage(PostMessages.TitleInputPlaceholder)}
            maxLength={MAX_POST_TITLE_LENGTH}
            style={{ marginBottom: 0 }}
            autoComplete="off"
            hasFeedback={false}
            autosize={true}
            {...textAreaNewlineRemovalProps('title', setFieldValue)}
          />

          <SubTitleInput
            name="subTitle"
            placeholder={formatMessage(PostMessages.SubtitleInputPlaceholder)}
            maxLength={MAX_POST_TITLE_LENGTH}
            style={{ marginTop: -5 }}
            autoComplete="off"
            autosize={true}
            {...textAreaNewlineRemovalProps('subTitle', setFieldValue)}
          />

          <CreatePostEditor
            initialValue={formValues.body}
            onChange={model => onEditorChange(model)}
            onImageUpload={onImageUpload}
            postOid={postOid}
            error={bodyError}
          />
        </LeftCol>

        <RightCol xl={7} lg={9} md={24}>
          {formValues.ageRestricted !== null &&
            <Checkbox
              name="ageRestricted"
              label={<FormattedMessage {...PostMessages.AgeRatingCheckboxLabel}/>}
              extra={<FormattedMessage {...PostMessages.AgeRatingCheckboxExtra}/>}
              defaultChecked={formValues.ageRestricted}
              checked={formValues.ageRestricted}
            >
              <FormattedMessage {...PostMessages.AgeRatingCheckboxText}/>
            </Checkbox>
          }

          <Checkbox
            name="disableComments"
            label={<FormattedMessage {...PostMessages.CommentsCheckboxLabel}/>}
            defaultChecked={formValues.disableComments}
            checked={formValues.disableComments}
          >
            <FormattedMessage {...PostMessages.CommentsCheckboxText}/>
          </Checkbox>

          <InputField
            name="canonicalUrl"
            type="url"
            label={<FormattedMessage {...PostMessages.CanonicalUrlInputLabel}/>}
            extra={<FormattedMessage {...PostMessages.CanonicalUrlInputDescription} values={{learnMore}}/>}
            placeholder={intl.formatMessage(PostMessages.CanonicalUrlInputPlaceholder)}
            maxLength={MAX_CANONICAL_URL_LENGTH}
            value={formValues.canonicalUrl || undefined}
          />

          <Button {...btnProps} type="primary" onClick={onNextClick} block={true} loading={isValidating}>
            <FormattedMessage {...SharedComponentMessages.NextBtnText}/>
          </Button>
        </RightCol>
      </Row>
    </StyledViewWrapper>
  );
};

export const CreatePost = compose(
  injectIntl,
)(CreatePostFormComponent) as React.ComponentClass<ParentProps>;
