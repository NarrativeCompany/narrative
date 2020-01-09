import * as React from 'react';
import { compose } from 'recompose';
import { NicheCard } from '../components/NicheCard';
import { NicheCardForm } from '../components/NicheCardForm';
import { Form, Input } from 'antd';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import { BidCardBackMessages } from '../../../shared/i18n/BidCardBackMessages';
import { FormattedMessage } from 'react-intl';

const FormItem = Form.Item;

// tslint:disable no-any
interface ParentProps {
  toggleCard: () => any;
}
// tslint:enable no-any

type Props =
  ParentProps &
  InjectedIntlProps;

const BidCardBackComponent: React.SFC<Props> = (props) => {
  const { toggleCard, intl } = props;

  return (
    <NicheCard height={315}>
      <NicheCardForm
        title={<FormattedMessage {...BidCardBackMessages.Title}/>}
        subTitle="2,863 NRVE"
        btnText={<FormattedMessage {...BidCardBackMessages.BtnText}/>}
        btnType="primary"
        toggleCard={toggleCard}
      >
        <FormItem
          label={intl.formatMessage(BidCardBackMessages.FormItemLabel)}
          style={{width: '100%'}}
          help={intl.formatMessage(BidCardBackMessages.FormItemHelp)}
          colon={false}
        >
          <Input
            name="bid"
            placeholder={intl.formatMessage(BidCardBackMessages.FormItemPlaceholder)}
          />
        </FormItem>
      </NicheCardForm>
    </NicheCard>
  );
};

export const AuctionCardBack = compose(
  injectIntl
)(BidCardBackComponent) as React.ComponentClass<ParentProps>;
