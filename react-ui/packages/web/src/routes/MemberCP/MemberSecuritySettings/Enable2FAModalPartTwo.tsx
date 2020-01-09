import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { Enable2FAMessages } from '../../../shared/i18n/Enable2FAMessages';
import { Block } from '../../../shared/components/Block';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { Paragraph } from '../../../shared/components/Paragraph';
import { ModalPrintButton } from '../../../shared/components/ModalPrintButton';
import { Divider } from 'antd';
import { Link } from '../../../shared/components/Link';

export interface Enable2FAModalPartTwoProps {
  backupCodes: number[];
}

interface Props extends Enable2FAModalPartTwoProps {
  dismiss: () => void;
}

function formatBackupCode(backupCode: number): string {
  return ('000000' + backupCode).slice(-6);
}

export const Enable2FAModalPartTwo: React.SFC<Props> = (props) => {
  const { backupCodes, dismiss } = props;

  return (
    <React.Fragment>
      <Paragraph marginBottom="large">
        <strong>
          <FormattedMessage {...Enable2FAMessages.StepThreeTitle}/>
        </strong>

        <FormattedMessage {...Enable2FAMessages.StepThreeMessage}/>
      </Paragraph>

      <Paragraph marginBottom="large" textAlign="center">
        <strong>
          <FormattedMessage {...Enable2FAMessages.BackupCodes}/>
        </strong>
      </Paragraph>

      <FlexContainer justifyContent="space-between" style={{margin: '0 auto 30px', maxWidth: 300}}>
        <Block>
          {backupCodes.slice(0, 5).map((backupCode) => (
            <Block key={backupCode} size="large" color="light">{formatBackupCode(backupCode)}</Block>
          ))}
        </Block>
        <Block color="light" style={{textAlign: 'right'}}>
          {backupCodes.slice(5).map((backupCode) => (
            <Block key={backupCode} size="large" color="light">{formatBackupCode(backupCode)}</Block>
          ))}
        </Block>
      </FlexContainer>

      <FlexContainer alignItems="center" column={true}>
        <ModalPrintButton type="primary">
          <FormattedMessage {...Enable2FAMessages.Print}/>
        </ModalPrintButton>

        <Divider/>

        <Link.Anchor onClick={dismiss} color="light">
          <FormattedMessage {...Enable2FAMessages.Close}/>
        </Link.Anchor>
      </FlexContainer>
    </React.Fragment>
  );
};
