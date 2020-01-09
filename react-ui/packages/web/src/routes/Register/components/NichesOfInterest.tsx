import * as React from 'react';
import { compose, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { Button } from '../../../shared/components/Button';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { Paragraph } from '../../../shared/components/Paragraph';
import { NicheSearchWrapper } from '../../../shared/components/niche/NicheSearchWrapper';
import { RegisterMessages } from '../../../shared/i18n/RegisterMessages';
import { NicheSearch } from '../../../shared/components/niche/NicheSearch';
import { SuggestedNiches } from '../../../shared/components/niche/SuggestedNiches';
import { FormMethodError } from '../../../shared/components/FormMethodError';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import { MethodError, Niche, withNichesOfInterest, WithNichesOfInterestProps } from '@narrative/shared';

interface WithProps {
  niches: Niche[];
  showNicheList: boolean;
}

interface ParentProps extends MethodError {
  onAddSelectedNiche: (niche: Niche) => void;
  onRemoveSelectedNiche: (niche: Niche) => void;
  selectedNiches: Niche[];
  isSubmitting: boolean;
}

type Props =
  ParentProps &
  WithProps;

export const NichesOfInterestComponent: React.SFC<Props> = (props) => {
  const {
    onAddSelectedNiche,
    onRemoveSelectedNiche,
    selectedNiches,
    niches,
    showNicheList,
    methodError,
    isSubmitting,
  } = props;

  const boldText = (
    <strong>
      <FormattedMessage {...RegisterMessages.NichesOfInterestSectionDescriptionBold}/>
    </strong>
  );

  return (
    <NicheSearchWrapper
      title={RegisterMessages.NichesOfInterestPageHeaderTitle}
      description={RegisterMessages.NichesOfInterestPageHeaderDescription}
      cancelLink={SharedComponentMessages.Cancel}
    >
      {methodError && <FormMethodError methodError={methodError}/>}

      <SectionHeader
        title={<FormattedMessage{...RegisterMessages.NichesOfInterestSectionTitle}/>}
        description={<FormattedMessage{...RegisterMessages.NichesOfInterestSectionDescription} values={{ boldText }}/>}
        style={{ border: 'none', paddingBottom: 0 }}
      />
      <NicheSearch
        onSelectNiche={(niche) => onAddSelectedNiche(niche)}
        selectedNiches={selectedNiches}
        onRemoveSelectedNiche={(niche) => onRemoveSelectedNiche(niche)}
        nicheCountSuccessMessage={RegisterMessages.NicheCountSuccessMessage}
      />

      {showNicheList &&
      <React.Fragment>
        <Paragraph color="light" style={{ marginBottom: 10 }}>
          <FormattedMessage {...RegisterMessages.PopularNichesSectionTitle}/>
        </Paragraph>

        <SuggestedNiches
          suggestedNiches={niches}
          onAddSelectedNiche={niche => onAddSelectedNiche(niche)}
        />
      </React.Fragment>}

      <FlexContainer>
        <Button type="primary" size="large" htmlType="submit" loading={isSubmitting} style={{ marginBottom: 50 }}>
          <FormattedMessage {...RegisterMessages.SubmitBtnText}/>
        </Button>
      </FlexContainer>
    </NicheSearchWrapper>
  );
};

export const NichesOfInterest = compose(
  withNichesOfInterest,
  withProps((props: ParentProps & WithNichesOfInterestProps) => {
    const { nichesOfInterest, selectedNiches } = props;

    const niches = nichesOfInterest
      .filter(niche => !selectedNiches.find(n => niche && niche.oid === n.oid));
    const showNicheList = niches.length > 0;

    return { niches, showNicheList };
  })
)(NichesOfInterestComponent) as React.ComponentClass<ParentProps>;
