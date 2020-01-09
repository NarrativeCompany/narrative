import { AsyncStorage } from 'react-native';

const tokenKey: string = '@narrative/token';

export async function storeAuthToken (token: string) {
  try {
    await AsyncStorage.removeItem(tokenKey);
    await AsyncStorage.setItem(tokenKey, token);
  } catch (err) {
    throw new Error(`storeAuthToken: ${err}`);
  }
}

export async function getAuthToken () {
  const token = await AsyncStorage.getItem(tokenKey);

  if (!token) {
    return null;
  }

  return token;
}