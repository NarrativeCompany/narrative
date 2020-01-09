import * as React from 'react';
import { compose } from 'recompose';
import { StepWrapper } from './FormStepWrapper';
import { Col, Row } from 'antd';
import { ColProps } from 'antd/lib/grid';
import { FormField } from '../../../shared/components/FormField';
import { Button } from '../../../shared/components/Button';
import { Link } from '../../../shared/components/Link';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { NicheNameAndDescStepMessages } from '../../../shared/i18n/NicheNameAndDescStepMessages';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import { SetFieldValue, textAreaNewlineRemovalProps } from '../../../shared/utils/removeNewlinesFromTextarea';
import styled from '../../../shared/styled';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';
import { maxNicheNameLength, ReputationLevel } from '@narrative/shared';

const { Input, TextArea } = FormField;

const StyledCol = styled<ColProps>(({...props}) => <Col {...props}/>)`
  margin-bottom: 15px;
`;

interface ParentProps {
  isSubmitting: boolean;
  setFieldValue: SetFieldValue;
}

type Props =
  ParentProps &
  InjectedIntlProps &
  WithExtractedCurrentUserProps;

export const NicheNameAndDescStepComponent: React.SFC<Props> = (props) => {
  const { intl, isSubmitting, setFieldValue, currentUserLoading, currentUser } = props;

  const termsOfService = <Link.Legal type="tos"/>;
  const acceptableUsePolicy = <Link.Legal type="aup"/>;

  let extraDescription;
  if (!currentUserLoading &&
    (!currentUser || !currentUser.reputation || currentUser.reputation.level !== ReputationLevel.HIGH)
  ){
    const boldText = (
      <strong>
        <FormattedMessage {...NicheNameAndDescStepMessages.PageHeaderDescriptionBold}/>
      </strong>
    );
    extraDescription = (
      <FormattedMessage
        {...NicheNameAndDescStepMessages.PageHeaderDescriptionYouCanSuggestOneNichePerDay}
        values={{boldText}}
      />
    );
  }

  const description = (
    <React.Fragment>
      <FormattedMessage
        {...NicheNameAndDescStepMessages.PageHeaderDescription}
        values={{termsOfService, acceptableUsePolicy}}
      />
      {extraDescription}
    </React.Fragment>
  );

  return (
    <StepWrapper
      title={<FormattedMessage {...NicheNameAndDescStepMessages.PageHeaderTitle}/>}
      description={description}
    >
      <Row gutter={16}>
        <StyledCol span={24}>
          <Input
            placeholder={intl.formatMessage(NicheNameAndDescStepMessages.NameFieldPlaceholder)}
            size="large"
            name="name"
            maxLength={maxNicheNameLength}
          />
        </StyledCol>

        <StyledCol span={24}>
          <TextArea
            placeholder={intl.formatMessage(NicheNameAndDescStepMessages.DefinitionFieldPlaceholder)}
            name="description"
            rows={4}
            maxLength={256}
            {...textAreaNewlineRemovalProps('description', setFieldValue)}
          />
        </StyledCol>

        <StyledCol span={24}>
          <FlexContainer justifyContent="center">
            <Button
              size="large"
              type="primary"
              style={{minWidth: 180}}
              loading={isSubmitting}
              htmlType="submit"
            >
              <FormattedMessage {...SharedComponentMessages.NextBtnText}/>
            </Button>
          </FlexContainer>
        </StyledCol>
      </Row>
    </StepWrapper>
  );
};

export const NicheNameAndDescStep = compose(
  injectIntl,
  withExtractedCurrentUser
)(NicheNameAndDescStepComponent) as React.ComponentClass<ParentProps>;
