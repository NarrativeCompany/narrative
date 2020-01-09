import * as React from 'react';
import { branch, compose, renderNothing } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { CreatePublicationMessages } from '../../../shared/i18n/CreatePublicationMessages';
import { withCurrentUserPublicationDiscount, WithCurrentUserPublicationDiscountProps } from '@narrative/shared';
import styled from 'styled-components';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { Heading } from '../../../shared/components/Heading';
import { Paragraph } from '../../../shared/components/Paragraph';

const DiscountDiv = styled(FlexContainer)`
  padding: 10px;
  border: 1px solid #dfdfdf;
  background-color: #F2794F;
  margin-top: 20px;
  margin-bottom: 20px;
  h4, p
  {
    color: white !important;
  }
`;

const PublicationDiscountBubbleComponent: React.SFC<{}> = () => {
  return (
    <DiscountDiv borderRadius="large" column={true}>
        <Heading size={4}>
          <FormattedMessage {...CreatePublicationMessages.PlanDiscountTitle}/>
        </Heading>
        <Paragraph>
          <FormattedMessage {...CreatePublicationMessages.PlanDiscountBody}/>
        </Paragraph>
    </DiscountDiv>
  );
};

export const PublicationDiscountBubble = compose(
  withCurrentUserPublicationDiscount,
  branch((props: WithCurrentUserPublicationDiscountProps) => props.loading || !props.eligibleForDiscount,
    renderNothing
  ),
)(PublicationDiscountBubbleComponent) as React.ComponentClass<{}>;
