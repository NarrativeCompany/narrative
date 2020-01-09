import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { StyledViewWrapper } from '../Post';
import { FormButtonGroup } from '../../../shared/components/FormButtonGroup';
import { FormMethodError } from '../../../shared/components/FormMethodError';
import { NicheSearch } from '../../../shared/components/niche/NicheSearch';
import { SuggestedNiches } from '../../../shared/components/niche/SuggestedNiches';
import { withRenderCurrentSlide } from '../../../shared/containers/withCarouselController';
import { PostMessages } from '../../../shared/i18n/PostMessages';
import {
  MethodError,
  Niche,
  PostInput,
  Publication,
  withNichesMostPostedToByCurrentUser,
  WithNichesMostPostedToByCurrentUserProps
} from '@narrative/shared';
import styled from '../../../shared/styled';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import { PageHeader } from '../../../shared/components/PageHeader';
import { Link } from '../../../shared/components/Link';
import { SelectField, SelectFields } from '../../../shared/components/SelectField';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import { MemberLink } from '../../../shared/components/user/MemberLink';
import { Loading } from '../../../shared/components/Loading';
import { SelectedPublicationWarning } from './SelectedPublicationWarning';

const nicheSelectionCount = 3;

const InnerWrapper = styled.div`
  min-width: 100%;
`;

interface WithProps {
  suggestedNiches: Niche[];
  showSuggestedNiches: boolean;
}

interface ParentProps extends MethodError {
  onPrevClick: () => void;
  onNextClick: () => void;
  onAddSelectedNiche: (niche: Niche) => void;
  onRemoveSelectedNiche: (niche: Niche) => void;
  selectedNiches: Niche[];
  publishToPublication?: Publication;
  authorPersonalJournalOid: string;
  currentSlide: number;
  isSubmitting: boolean;
  availablePublications: Publication[];
  formValues: PostInput;
}

type Props =
  ParentProps &
  InjectedIntlProps &
  WithExtractedCurrentUserProps &
  WithProps;

const SelectChannelsComponent: React.SFC<Props> = (props) => {
  const {
    onPrevClick,
    onAddSelectedNiche,
    onRemoveSelectedNiche,
    selectedNiches,
    suggestedNiches,
    showSuggestedNiches,
    isSubmitting,
    methodError,
    publishToPublication,
    authorPersonalJournalOid,
    availablePublications,
    currentUserPersonalJournalOid,
    currentUser,
    formValues,
    intl: { formatMessage }
  } = props;

  if (!currentUser) {
    // todo:error-handling: This should never happen, so we should report this to the server
    return null;
  }

  const isCurrentUserAuthor = currentUserPersonalJournalOid === authorPersonalJournalOid;

  const journalLink = (
    <MemberLink user={currentUser} targetBlank={true} hideBadge={true}>
      <FormattedMessage {...PostMessages.JournalLabel}/>
    </MemberLink>
  );

  const nicheLink = (
    <Link.About type="niche"/>
  );
  const nichesLink = (
    <Link.About type="niches"/>
  );

  function getChannelSelectionOptions(): SelectFields {

    const channelSelectFields: SelectFields = [];

    const publicationMapper = (value: Publication) => ({
      value: value.oid,
      text: value.name
    });

    // bl: if the post is published to a publication, then only put the Publication in the list
    if (!formValues.draft && publishToPublication) {
      channelSelectFields.push(publicationMapper(publishToPublication));
      return channelSelectFields;
    }

    // journal selection
    channelSelectFields.push({
      value: authorPersonalJournalOid,
      text: <FormattedMessage {...PostMessages.ChannelSelectionYourPublicJournalLabel}/>
    });

    // user's publication selections
    const publicationFields: SelectFields = availablePublications
      && availablePublications.map(publicationMapper);
    channelSelectFields.push(...publicationFields);

    // none - not publishing to their journal or a publication
    channelSelectFields.push({
      value: '',
      text: <FormattedMessage {...PostMessages.ChannelSelectionNoneLabel}/>
    });

    return channelSelectFields;
  }

  return (
    <StyledViewWrapper>
      <InnerWrapper>
        <FormButtonGroup
          linkText={<FormattedMessage {...PostMessages.EditPostBtnText}/>}
          hasBackArrow={true}
          btnText={<FormattedMessage {...PostMessages.PublishBtnText}/>}
          linkProps={{ onClick: onPrevClick }}
          btnProps={{ htmlType: 'submit', style: { minWidth: 180 }, loading: isSubmitting }}
          style={{ margin: '10px 0 40px' }}
        />
        {methodError && <FormMethodError methodError={methodError}/>}
        <PageHeader
          title={<FormattedMessage {...PostMessages.ChannelSelectionPageTitle}/>}
          description={<FormattedMessage {...PostMessages.ChannelSelectionPageDescription}/>}
          size={2}
          style={{ marginBottom: 15 }}
        />
        <SectionHeader
          title={<FormattedMessage {...PostMessages.ChannelSelectionTitle}/>}
          description={
            isCurrentUserAuthor
            ? <FormattedMessage
               {...PostMessages.ChannelDescription}
               values={{ journalLink, nicheLink }}
            />
            : null
          }
          size={'lg'}
          noBottomBorder={true}
          style={{ marginBottom: 15, borderBottomStyle: 'none' }}
        />
        <SelectField
          disabled={!!publishToPublication && !formValues.draft}
          name="publishToPrimaryChannel"
          label={<FormattedMessage {...PostMessages.ConnectedNichesChannelSelectionLabel}/>}
          style={{ width: '100%' }}
          selectFields={getChannelSelectionOptions()}
          placeholder={formatMessage(PostMessages.ChannelSelectionDefaultChoiceLabel)}
        />
        {formValues.draft
          && formValues.publishToPrimaryChannel
          && formValues.publishToPrimaryChannel !== authorPersonalJournalOid
          &&
          <SelectedPublicationWarning publicationOid={formValues.publishToPrimaryChannel}/>
        }
        <SectionHeader
          title={<FormattedMessage {...PostMessages.ConnectedNichesSectionTitle}/>}
          description={<FormattedMessage
            {...(isCurrentUserAuthor
              ? PostMessages.ConnectedNichesSectionDescription
              : PostMessages.ConnectedNichesSectionDescriptionEditor
            )}
            values={{ nichesLink }}
          />}
          size={'lg'}
          noBottomBorder={true}
          style={{ marginBottom: 15 }}
        />
        <NicheSearch
          onSelectNiche={(niche) => onAddSelectedNiche(niche)}
          selectedNiches={selectedNiches}
          onRemoveSelectedNiche={(niche) => onRemoveSelectedNiche(niche)}
          nicheSelectionCount={nicheSelectionCount}
        />
        {showSuggestedNiches &&
        <SuggestedNiches
          title={SharedComponentMessages.SuggestedNichesSectionTitle}
          suggestedNiches={suggestedNiches}
          onAddSelectedNiche={niche => onAddSelectedNiche(niche)}
        />}
      </InnerWrapper>
    </StyledViewWrapper>
  );
};

export const SelectChannels = compose(
  withRenderCurrentSlide(1),
  withExtractedCurrentUser,
  branch((props: WithExtractedCurrentUserProps) => !!props.currentUserLoading,
    renderComponent(() => <Loading />)
  ),
  withNichesMostPostedToByCurrentUser,
  injectIntl,
  withProps((props: ParentProps & WithNichesMostPostedToByCurrentUserProps) => {
    const { recentlyUsedNiches, selectedNiches } = props;

    // tslint:disable no-string-literal
    const suggestedNiches = recentlyUsedNiches
      .filter(niche => !selectedNiches.find(n => niche && niche['oid'] === n.oid));
    // tslint:enable no-string-literal
    const showSuggestedNiches = suggestedNiches.length > 0 && selectedNiches.length < nicheSelectionCount;

    return { suggestedNiches, showSuggestedNiches };
  })
)(SelectChannelsComponent) as React.ComponentClass<ParentProps>;
