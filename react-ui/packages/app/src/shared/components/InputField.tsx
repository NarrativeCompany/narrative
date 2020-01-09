import * as React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { InputItem } from 'antd-mobile-rn';
import { FormikProps } from 'formik';
import { InputItemProps } from 'antd-mobile-rn/lib/input-item/index.native';

interface ParentProps {
  fieldName: string
}

type Props =
  ParentProps &
  FormikProps<any> &
  InputItemProps;

export const InputField: React.SFC<Props> = (props) => {
  const { errors, touched, values, name, fieldName, setFieldValue } = props;
  const errorMessage = touched[fieldName] && errors[fieldName];

  return (
    <View>
      <InputItem
        name={name}
        type={props.type || 'text'}
        clear={props.clear || true}
        error={!!errorMessage}
        placeholder={props.placeholder || undefined}
        autoCapitalize={props.autoCapitalize || 'none'}
        value={values[fieldName]}
        onErrorClick={() => alert(errorMessage)}
        onChange={(value: string) => setFieldValue(fieldName, value)}
      />
      {errorMessage && <Text style={styles.error}>{errorMessage}</Text>}
    </View>
  );
};

const styles = StyleSheet.create({
  error: {
    color: '#f50',
    marginLeft: 15,
    marginTop: 3,
    fontSize: 11
  }
});