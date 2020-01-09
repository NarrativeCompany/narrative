import { updateAuthenticationState } from './updateAuthState';
import { setErrorState } from './setErrorState';
import { clearErrorState } from './clearErrorState';

export const resolvers = {
  Mutation: {
    updateAuthenticationState,
    setErrorState,
    clearErrorState
  }
};
