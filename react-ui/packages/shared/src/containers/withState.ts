import { InferableComponentEnhancerWithProps, withState as recomposeWithState } from 'recompose';

// tslint:disable interface-over-type-literal
export type WithStateProps<State, Props = {}> = {
  state: State;
  setState: SetStateViaValueOrFunction<State, Props>;
};

export type SetStateViaValueOrFunction<State, Props> =
  (input: State | ((currentState: State, props: Props) => State), callback?: () => void) => void;

export function withState<State, Props = {}> (
  initialState: State | ((props: Props) => State)
): InferableComponentEnhancerWithProps<{state: State, setState: (state: State) => State}, Props> {
  return recomposeWithState('state', 'setState', initialState);
}
