import { buildApolloCacheErrorState, buildEmptyErrorState } from '../resolvers';

export const errorStateDefault = buildApolloCacheErrorState(buildEmptyErrorState());
