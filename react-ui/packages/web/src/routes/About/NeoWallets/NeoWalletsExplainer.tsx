import * as React from 'react';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { DetailsGradientBox } from '../../../shared/components/DetailsGradientBox';
import { ViewWrapper } from '../../../shared/components/ViewWrapper';
import { AboutPageHeader } from '../components/AboutPageHeader';
import { compose } from 'recompose';
import { AboutSection } from '../components/AboutSection';
import { NeoWalletsExplainerMessages } from '../../../shared/i18n/NeoWalletsExplainerMessages';
import { WithNeoWalletsProps, withNeoWallets, NeoWallet, NeoWalletType } from '@narrative/shared';
import { viewWrapperPlaceholder, withLoadingPlaceholder } from '../../../shared/utils/withLoadingPlaceholder';
import { NeoAddressLink } from '../../../shared/components/neo/NeoAddressLink';
import { EnhancedNeoWalletType } from '../../../shared/enhancedEnums/neoWalletType';
import { NeoAssetLink } from '../../../shared/components/neo/NeoAssetLink';
import styled from 'styled-components';

type Props = InjectedIntlProps & WithNeoWalletsProps;

const Label = styled.span`
  font-weight: bold;
`;

const MetadataWrapper = styled.div`
  > * {
    display: inline;
  }
`;

const NeoWalletsExplainerComponent: React.SFC<Props> = (props) => {
  const { neoWallets, intl: { formatMessage }} = props;

  const neoWalletSections: React.ReactElement[] = [];

  const neoWalletsByType = new Map<NeoWalletType, NeoWallet[]>();

  neoWallets.forEach((neoWallet: NeoWallet) => {
    let neoWalletsForType = neoWalletsByType.get(neoWallet.type);
    if (!neoWalletsForType) {
      neoWalletsByType.set(neoWallet.type, neoWalletsForType = []);
    }
    neoWalletsForType.push(neoWallet);
  });

  neoWalletsByType.forEach((neoWalletsForType: NeoWallet[], type: NeoWalletType) => {
    const neoWalletType = EnhancedNeoWalletType.get(type);
    neoWalletSections.push(
      <AboutSection
        title={neoWalletType.name}
        message={neoWalletType.description}>
        {neoWalletsForType.map((neoWallet: NeoWallet, index: number) => {
          return (
            <React.Fragment key={index}>
              <MetadataWrapper>
                <Label>
                  {neoWallet.monthForDisplay ? neoWallet.monthForDisplay + ': ' : ''}
                </Label>
                <NeoAddressLink address={neoWallet.neoAddress} showFull={true}/>
              </MetadataWrapper>
              {neoWalletType.isNarrativeCompany() && neoWallet.extraNeoAddress &&
                <MetadataWrapper>
                  <NeoAddressLink address={neoWallet.extraNeoAddress} showFull={true}/>
                </MetadataWrapper>
              }
              {neoWalletType.isNrveSmartContract() && neoWallet.scriptHash &&
                <MetadataWrapper>
                  <Label>
                    <FormattedMessage {...NeoWalletsExplainerMessages.ScriptHash}/>
                  </Label>
                  <NeoAssetLink scriptHash={neoWallet.scriptHash} showFull={true}/>
                </MetadataWrapper>
              }
            </React.Fragment>
          );
        })}
      </AboutSection>
    );
  });

  return (
    <ViewWrapper
      maxWidth={890}
      style={{ paddingTop: 75 }}
      gradientBox={<DetailsGradientBox color="darkBlue" size="large"/>}
    >
      <AboutPageHeader
        seoTitle={formatMessage(NeoWalletsExplainerMessages.SEOTitle)}
        title={<FormattedMessage {...NeoWalletsExplainerMessages.PageHeaderTitle}/>}
        description={<FormattedMessage {...NeoWalletsExplainerMessages.PageHeaderDescription}/>}
      />

      <AboutSection message={NeoWalletsExplainerMessages.Description}/>

      {neoWalletSections.map((neoWalletSection, i) => {
        return {...neoWalletSection, key: i};
      })}

    </ViewWrapper>
  );
};

export default compose(
  injectIntl,
  withNeoWallets,
  withLoadingPlaceholder(viewWrapperPlaceholder())
)(NeoWalletsExplainerComponent) as React.ComponentClass<{}>;
