import * as React from 'react';
import { compose } from 'recompose';
import { View } from 'react-native';
import { List, Button, WingBlank, WhiteSpace } from 'antd-mobile-rn';
import { InputField } from '../../shared/components/InputField';
import { PageHeader } from '../../shared/components/PageHeader';
import { withFormik, FormikProps } from 'formik';
import { withUserId, WithUserIdProps } from '../../shared/containers/withUserId';
import {
  withSuggestNicheOptions,
  WithSuggestNicheProps ,
  SuggestNicheFormValues,
  suggestNicheFormUtil,
  ApiOpts
} from '@narrative/shared';

// tslint:disable no-any
interface ParentProps {
  dismiss: () => any;
}
// tslint:enable no-any

type Props =
  ParentProps &
  FormikProps<SuggestNicheFormValues> &
  WithSuggestNicheProps &
  WithUserIdProps;

const SuggestNicheComponent: React.SFC<Props> = (props) => {
  const { dismiss, handleSubmit } = props;

  return (
    <View>
      <WhiteSpace size="lg"/>
      <WhiteSpace size="lg"/>
      <WhiteSpace size="lg"/>
      <PageHeader title="Suggest New Niche"/>
      <List>
          <InputField
            {...props}
            fieldName="name"
            placeholder="name"
          />

          <InputField
            {...props}
            fieldName="description"
            placeholder="description"
          />
      </List>

      <WingBlank size="lg">
        <View style={{marginTop: 15, flexDirection: 'row'}}>
          <Button
            style={{marginLeft: 'auto', marginRight: 5}}
            onClick={dismiss}
          >
            Cancel
          </Button>

          <Button
            style={{marginLeft: 5}}
            onClick={handleSubmit}
            type="primary"
          >
            Submit
          </Button>
        </View>
      </WingBlank>
    </View>
  );
};

export const SuggestNiche = compose<Props, any>(
  withUserId,
  withSuggestNicheOptions(ApiOpts.REST),
  withFormik<WithSuggestNicheProps & WithUserIdProps & ParentProps, SuggestNicheFormValues>({
    ...suggestNicheFormUtil,
    handleSubmit: async (values, {props}) => {
      try {
        const input = {
          ...values,
          userId: props.userId
        };
        await props.suggestNiche({input});
        props.dismiss();
      } catch (err) {
        // tslint:disable-next-line no-console
        console.log(`suggest niche submit handler: ${err}`);
      }
    }
  })
)(SuggestNicheComponent) as React.ComponentClass<ParentProps>;