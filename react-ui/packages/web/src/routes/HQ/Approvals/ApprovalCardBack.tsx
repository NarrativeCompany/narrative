import * as React from 'react';
import { Icon } from 'antd';
import { NicheCard } from '../components/NicheCard';
import { ReferendumRejectReasonForm } from '../../../shared/components/referendum/ReferendumRejectReasonForm';
import { FormattedMessage } from 'react-intl';
import { ApprovalCardBackMessages } from '../../../shared/i18n/ApprovalCardBackMessages';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { IconProps } from 'antd/lib/icon';
import { Heading } from '../../../shared/components/Heading';
import { Referendum } from '@narrative/shared';
import styled from '../../../shared/styled';

const FormWrapper = styled<FlexContainerProps>(FlexContainer)`
  width: 100%;
  height: 100%;
`;

const HeadingWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 5px;
  width: 100%;
`;

// tslint:disable-next-line no-any
const CloseButton = styled<IconProps & {theme: any}>(Icon)`
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 18px;
  
  &:hover {
    color: ${props => props.theme.primaryRed};
  }
`;

interface ParentProps {
  referendum: Referendum;
  // tslint:disable-next-line no-any
  toggleCard: () => any;
}

export const ApprovalCardBack: React.SFC<ParentProps> = (props) => {
  const { referendum, toggleCard } = props;

  return (
    <NicheCard height={400}>
      <FormWrapper column={true}>
        <CloseButton type="close" onClick={toggleCard}/>

        <HeadingWrapper column={true}>
          <Heading size={4} weight={600}>
            <FormattedMessage {...ApprovalCardBackMessages.Title}/>
          </Heading>
        </HeadingWrapper>

        <ReferendumRejectReasonForm referendum={referendum} dismissForm={toggleCard} />
      </FormWrapper>
    </NicheCard>
  );
};
