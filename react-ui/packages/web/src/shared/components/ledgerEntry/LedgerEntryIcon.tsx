import * as React from 'react';
import { LedgerEntryProps } from './LedgerEntryListItem';
import styled from '../../styled/index';
import { CustomIcon } from '../CustomIcon';
import { EnhancedLedgerEntryType } from '../../enhancedEnums/ledgerEntryType';

const StyledIcon = styled(CustomIcon)`
  margin-top: 8px;
`;

export const LedgerEntryIcon: React.SFC<LedgerEntryProps> = (props) => {
  const { ledgerEntry } = props;

  const type = EnhancedLedgerEntryType.get(ledgerEntry.type);

  return <StyledIcon size={18} type={type.ledgerEntryIcon} />;
};
