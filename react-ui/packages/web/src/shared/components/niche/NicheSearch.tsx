import * as React from 'react';
import { compose, withHandlers, withProps } from 'recompose';
import { Select } from 'antd';
import { SelectProps } from 'antd/lib/select';
import { FormattedMessage } from 'react-intl';
import { FormControl } from '../FormControl';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { ConnectedNicheCount } from './ConnectedNicheCount';
import { NicheTagRow } from './NicheTagRow';
import { NicheTag } from './NicheTag';
import { Niche, withFindActiveNiches, WithFindActiveNichesProps, withState, WithStateProps } from '@narrative/shared';
import { PostMessages } from '../../i18n/PostMessages';
import styled from '../../styled';

const SearchWrapper = styled<FlexContainerProps>(FlexContainer)`
  @media screen and (max-width: 575px) {
    flex-direction: column;
    align-items: flex-start;
  }
`;

export const SearchInput = styled<SelectProps>((props) => <Select {...props}/>)`
  flex: 2;
  
  .ant-select-selection {
    cursor: text;
  }
  
  .ant-select-selection,
  .ant-select-selection__rendered {
    height: 50px;
  }

  .ant-select-selection__rendered {
    line-height: 50px;
  }
`;

interface State {
  name?: string;
}

interface WithHandlers {
  handleSelectChange: (nicheName: string) => void;
}

interface WithProps {
  searchResults: Niche[];
}

interface ParentProps {
  selectedNiches: Niche[];
  onSelectNiche: (niche: Niche) => void;
  onRemoveSelectedNiche: (niche: Niche) => void;
  nicheSelectionCount?: number;
  nicheCountSuccessMessage?: FormattedMessage.MessageDescriptor;
}

type NicheSearchProps =
  ParentProps &
  WithStateProps<State> &
  WithHandlers &
  WithProps;

export const NicheSearchComponent: React.SFC<NicheSearchProps> = (props) => {
  const {
    searchResults,
    selectedNiches,
    handleSelectChange,
    onRemoveSelectedNiche,
    nicheSelectionCount,
    nicheCountSuccessMessage,
    state,
    setState
  } = props;

  const options = searchResults.map((niche: Niche) =>
    <Select.Option key={niche.oid} value={niche.oid}>{niche.name}</Select.Option>);

  return (
    <React.Fragment>
      <FormControl>
        <SearchWrapper alignItems="center">
          <SearchInput
            placeholder={<FormattedMessage {...PostMessages.NicheSearchPlaceholder}/>}
            showSearch={true}
            showArrow={false}
            defaultActiveFirstOption={false}
            filterOption={false}
            value={state.name}
            onSearch={value => setState(ss => ({ ...ss, name: value && value.length ? value : undefined }))}
            onChange={(value: string) => handleSelectChange(value)}
            disabled={nicheSelectionCount ? selectedNiches.length === nicheSelectionCount : false}
          >
            {options}
          </SearchInput>

          <ConnectedNicheCount totalCount={selectedNiches.length} successMessage={nicheCountSuccessMessage}/>
        </SearchWrapper>
      </FormControl>

      <NicheTagRow>
        {selectedNiches.length > 0 && selectedNiches.map((niche: Niche) => (
          <NicheTag
            key={niche.oid}
            niche={niche}
            color="mediumGray"
            onClick={(n: Niche) => onRemoveSelectedNiche(n)}
          />
        ))}
      </NicheTagRow>
    </React.Fragment>
  );
};

export const NicheSearch = compose(
  withState<State>({}),
  withProps((props: WithStateProps<State>) => ({
    name: props.state.name
  })),
  withFindActiveNiches,
  withProps((props: WithFindActiveNichesProps & ParentProps & WithStateProps<State>) => {
    const { activeNiches, loading, selectedNiches } = props;

    const filteredResults =
      activeNiches &&
      activeNiches
        .filter(niche => !selectedNiches.find(n => niche && niche.oid === n.oid)) || [];

    return { loading, searchResults: filteredResults };
  }),
  withHandlers({
    handleSelectChange: (props: ParentProps & WithProps & WithStateProps<State>) => (nicheOid: string) => {
      const { searchResults, setState, onSelectNiche } = props;

      const niche = searchResults.find(n => n.oid === nicheOid);

      if (niche) {
        onSelectNiche(niche);
        setState(ss => ({ ...ss, name: undefined }));
      }
    }
  })
)(NicheSearchComponent) as React.ComponentClass<ParentProps>;
