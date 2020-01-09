import * as React from 'react';
import { RewardPeriod, WithExtractedRewardPeriodsProps } from '@narrative/shared';
import { Select } from 'antd';
import { branch, compose, renderComponent } from 'recompose';
import { LoadingProps } from '../../utils/withLoadingPlaceholder';
import styled from '../../styled';
import { SelectProps, SelectValue } from 'antd/lib/select';
import { RewardsMessages } from '../../i18n/RewardsMessages';
import { FormattedMessage } from 'react-intl';
import { Paragraph } from '../Paragraph';

const RewardPeriodSelectInput = styled<SelectProps>((props) => <Select {...props}/>)`
  width: 100%;
  margin-bottom: 20px !important;
`;

export interface RewardPeriodSelectorProps {
  month: string;
  includeExcludedMonthsNote?: boolean;
  onChange: (value: SelectValue) => void;
}

export type RewardPeriodSelectorParentProps = RewardPeriodSelectorProps & WithExtractedRewardPeriodsProps;

const RewardPeriodSelectorComponent: React.SFC<RewardPeriodSelectorParentProps> = (props) => {
  const { rewardPeriods, month, includeExcludedMonthsNote, onChange } = props;

  return (
    <React.Fragment>
      <RewardPeriodSelectInput
        defaultValue={month || rewardPeriods[0].yearMonth}
        onChange={onChange}
      >
          {rewardPeriods.map((rewardPeriod: RewardPeriod) => (
            <Select.Option key={rewardPeriod.yearMonth} value={rewardPeriod.yearMonth}>
              {rewardPeriod.name}
            </Select.Option>
          ))}
      </RewardPeriodSelectInput>
      {includeExcludedMonthsNote &&
        <Paragraph color="lightGray" style={{marginTop: -10, marginBottom: 20}}>
          <FormattedMessage {...RewardsMessages.MonthsWithNoRewardsExcludedNote}/>
        </Paragraph>
      }
    </React.Fragment>
  );
};

export const RewardPeriodSelector = compose(
  branch((props: LoadingProps) => props.loading,
    renderComponent(() => null)
  )
)(RewardPeriodSelectorComponent) as React.ComponentClass<RewardPeriodSelectorParentProps>;
