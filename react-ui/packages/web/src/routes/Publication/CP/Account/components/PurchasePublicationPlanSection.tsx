import * as React from 'react';
import { OpenPlanPurchaseModalHandler } from '../PublicationAccount';
import { PublicationPlanType } from '@narrative/shared';
import { FormattedMessage, MessageValue } from 'react-intl';
import styled from '../../../../../shared/styled';
import { Button, ButtonType } from '../../../../../shared/components/Button';
import { Section } from '../../../../../shared/components/Section';

interface Props extends OpenPlanPurchaseModalHandler {
  title: FormattedMessage.MessageDescriptor;
  description: FormattedMessage.MessageDescriptor;
  descriptionValues?: {[key: string]: MessageValue | JSX.Element};
  buttonText: FormattedMessage.MessageDescriptor;
  buttonTextValues?: {[key: string]: MessageValue | JSX.Element};
  buttonType: ButtonType;
  plan: PublicationPlanType;
  afterButton?: React.ReactNode;
}

const ButtonContainer = styled.div`
  margin-top: 15px;
`;

export const PurchasePublicationPlanSection: React.SFC<Props> = (props) => {
  const {
    title,
    description,
    descriptionValues,
    buttonText,
    buttonTextValues,
    buttonType,
    plan,
    openPurchasePlanModal,
    afterButton
  } = props;

  return (
    <Section
      title={<FormattedMessage {...title} />}
      description={<FormattedMessage {...description} values={descriptionValues} />}
    >
      <ButtonContainer>
        <Button
          type={buttonType}
          onClick={() => openPurchasePlanModal(plan)}
        >
          <FormattedMessage {...buttonText} values={buttonTextValues} />
        </Button>
      </ButtonContainer>

      {afterButton && <ButtonContainer>{afterButton}</ButtonContainer>}
    </Section>
  );
};
