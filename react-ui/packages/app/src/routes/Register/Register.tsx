import * as React from 'react';
import { compose } from 'recompose';
import { RouteComponentProps } from 'react-router-native';
import { ScrollView, Text } from 'react-native';
import { Button, List } from 'antd-mobile-rn';
import { InputField } from '../../shared/components/InputField';
import { withFormik, FormikProps } from 'formik';
import {
  withRegisterUser,
  WithRegisterUserProps,
  RegisterFormValues,
  registerFormUtil,
  normalizeErrors
} from '@narrative/shared';
import { storeAuthToken } from '../../shared/utils/asyncStorageUtil';
import { AppRoutes } from '../../shared/constants/routes';

type Props =
  FormikProps<RegisterFormValues> &
  WithRegisterUserProps &
  RouteComponentProps<{}>;

export const RegisterComponent: React.SFC<Props> = (props) => {
  const { handleSubmit } = props;

  return (
    <ScrollView
      style={{ flex: 1 }}
      automaticallyAdjustContentInsets={false}
      showsHorizontalScrollIndicator={false}
      showsVerticalScrollIndicator={false}
      contentContainerStyle={{ marginTop: 100 }}
    >
      <Text style={{fontSize: 36, fontWeight: '700', marginLeft: 15, marginBottom: 10}}>Sign up</Text>
      <List>
        <List.Item>
          <InputField
            {...props}
            fieldName="username"
            placeholder="username"
          />

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
        </List.Item>

        <List.Item>
          <Button
            style={{marginTop: 15}}
            onClick={handleSubmit}
            type="primary"
          >
            Register
          </Button>

          <Button
            style={{borderColor: 'transparent'}}
            onClick={() => props.history.push(AppRoutes.Login)}
            type="ghost"
          >
            Login
          </Button>
        </List.Item>
      </List>
    </ScrollView>
  );
};

export const Register = compose<Props, {}>(
  withRegisterUser,
  withFormik<Props, RegisterFormValues>({
    ...registerFormUtil,
    handleSubmit: async (values, {props, setErrors}) => {
      const input = {...values};
      const { error, token } = await props.registerUser({input});

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
)(RegisterComponent);
