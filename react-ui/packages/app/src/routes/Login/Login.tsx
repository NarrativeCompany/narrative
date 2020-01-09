import * as React from 'react';
import { compose } from 'recompose';
import { RouteComponentProps } from 'react-router-native';
import { ScrollView, Text } from 'react-native';
import { Button, List } from 'antd-mobile-rn';
import { InputField } from '../../shared/components/InputField';
import { withFormik, FormikProps } from 'formik';
import { storeAuthToken } from '../../shared/utils/asyncStorageUtil';
import {
  withLoginUser,
  WithLoginUserProps,
  LoginFormValues,
  loginFormikUtil,
  normalizeErrors
} from '@narrative/shared';
import { AppRoutes } from '../../shared/constants/routes';

type Props =
  FormikProps<LoginFormValues> &
  WithLoginUserProps &
  RouteComponentProps<{}>;

export const LoginComponent: React.SFC<Props> = (props) => {
  const { handleSubmit } = props;

  return (
    <ScrollView
      style={{ flex: 1 }}
      automaticallyAdjustContentInsets={false}
      showsHorizontalScrollIndicator={false}
      showsVerticalScrollIndicator={false}
      contentContainerStyle={{ marginTop: 100 }}
    >
      <Text style={{fontSize: 36, fontWeight: '700', marginLeft: 15, marginBottom: 10}}>Login</Text>
      <List>
        <InputField
          {...props}
          fieldName="email"
          placeholder="email"
        />

        <InputField
          {...props}
          fieldName="password"
          placeholder="password"
          type="password"
        />

        <List.Item>
          <Button
            style={{marginTop: 15}}
            onClick={handleSubmit}
            type="primary"
          >
            Login
          </Button>

          <Button
            style={{borderColor: 'transparent'}}
            onClick={() => props.history.push(AppRoutes.Register)}
            type="ghost"
          >
            Sign up
          </Button>
        </List.Item>
      </List>
    </ScrollView>
  );
};

export const Login = compose<Props, {}>(
  withLoginUser,
  withFormik<Props, LoginFormValues>({
    ...loginFormikUtil,
    handleSubmit: async (values, {props, setErrors}) => {
      const input = {...values};
      const { error, token } = await props.loginUser({input});

      if (error) {
        setErrors(normalizeErrors(error));
        return;
      }

      if (token) {
        await storeAuthToken(token);
        props.history.push(AppRoutes.BallotBox);
      }
    }
  })
)(LoginComponent);
