import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { Label } from '../settingsStyles';
import { FormattedMessage } from 'react-intl';
import { Link } from '../../../shared/components/Link';
import { Text } from '../../../shared/components/Text';
import { NeoAddressLink } from '../../../shared/components/neo/NeoAddressLink';
import { MemberNeoWalletMessages } from '../../../shared/i18n/MemberNeoWalletMessages';
import styled from '../../../shared/styled';
import { mediaQuery } from '../../../shared/styled/utils/mediaQuery';

const FieldContainer = styled<FlexContainerProps>(FlexContainer)`
  ${mediaQuery.sm_down`
    flex-direction: column;
    align-items: flex-start;
    margin-bottom: 10px;
  `};
`;

interface Props {
  neoAddress: string;
  status: React.ReactNode;
  updateHandler?: () => void;
  deleteHandler?: () => void;
}

export const MemberNeoWalletDetails: React.SFC<Props> = (props) => {
  const { neoAddress, status, updateHandler, deleteHandler } = props;

  const middot = (
    <Text style={{marginLeft: 7}}>
      &middot;
    </Text>
  );

  return (
    <React.Fragment>
      <FieldContainer alignItems="center">
        <Label uppercase={true} size={6}>
          <FormattedMessage {...MemberNeoWalletMessages.WalletAddress}/>
        </Label>

        <FlexContainer alignItems="center">
          <NeoAddressLink address={neoAddress} showFull={true}/>

          {updateHandler &&
            <React.Fragment>
              {middot}
              <Link.Anchor onClick={updateHandler} style={{ marginLeft: 7 }}>
                <FormattedMessage {...MemberNeoWalletMessages.ChangeWallet}/>
              </Link.Anchor>
            </React.Fragment>
          }

          {deleteHandler &&
            <React.Fragment>
              {middot}
              <Link.Anchor onClick={deleteHandler} style={{ marginLeft: 7 }}>
                <FormattedMessage {...MemberNeoWalletMessages.DeleteWallet}/>
              </Link.Anchor>
            </React.Fragment>
          }
        </FlexContainer>
      </FieldContainer>

      <FieldContainer alignItems="center">
        <Label uppercase={true} size={6}>
          <FormattedMessage {...MemberNeoWalletMessages.WalletStatus}/>
        </Label>

        {status}
      </FieldContainer>
    </React.Fragment>
  );
};
