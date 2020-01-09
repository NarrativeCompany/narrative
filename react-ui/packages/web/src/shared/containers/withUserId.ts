import { withProps } from 'recompose';
import * as jwt_decode from 'jwt-decode';
import { getAuthTokenState } from '../utils/authTokenUtils';

export function getUserId () {
  const token = getAuthTokenState().token;

  if (!token) {
    return null;
  }

  const tokenBody = jwt_decode(token);

  // tslint:disable no-string-literal
  const userId = tokenBody['jti'];
  // tslint:enable no-string-literal

  if (!userId) {
    return null;
  }

  return parseInt(userId, 10);
}

export interface WithUserIdProps {
  userId: number;
}

export const withUserId = withProps(() => ({userId: getUserId()}));
