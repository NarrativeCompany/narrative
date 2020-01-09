import * as React from 'react';
import * as moment from 'moment';
import styled, { css } from '../styled';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import { FormattedMessage } from 'react-intl';

interface StyleProps {
  inline?: boolean;
}

const TimeWrapper = styled.span<{hasEnded: boolean} & StyleProps>`
  display: inline-block;
  line-height: normal;
  ${p => !p.inline && css`width: 135px`};
  
  & > span {
    display: block;
    color: ${props => props.hasEnded ? props.theme.primaryGreen : props.theme.primaryRed}
  }
`;

interface State {
  hasEnded: boolean;
  days: number;
  hours: number;
  mins: number;
  secs: number;
}

interface Props extends StyleProps {
  endTime: string;
  // jw: making the children optional, so that this can support a basic representation.
  // tslint:disable-next-line no-any
  children?: (countDownState: State) => any;
  timeOnly?: boolean;
}

export class CountDown extends React.Component<Props, State> {
  // tslint:disable no-any
  interval: any;
  diffTime: any;
  duration: any;
  endTime: any;
  // tslint:enable no-any

  constructor(props: Props) {
    super(props);

    this.endTime = moment(this.props.endTime).unix();
    this.diffTime = this.endTime - moment().unix();
    this.duration = moment.duration(this.diffTime * 1000, 'milliseconds');

    // jw: let's initialize the state with proper values.
    this.state = this.calculateState();
  }

  // jw: this utility method will calculate the state object from current values.
  calculateState(): State {
    const days = moment.duration(this.duration).days();
    const hours = moment.duration(this.duration).hours();
    const mins = moment.duration(this.duration).minutes();
    const secs = moment.duration(this.duration).seconds();
    const hasEnded = this.duration <= 0;

    return {
      hasEnded,
      days,
      hours,
      mins,
      secs
    };
  }

  componentDidMount() {
    // jw: if the countdown has ended, let's short out and not setup the interval
    if (this.state.hasEnded) {
      return;
    }

    // jw: Let's setup a secondly interval to update the state and drive the UI
    this.interval = setInterval(() => {
      // jw: In order to update the state, we first need to calculate the effective duration shifted by our interval
      this.duration = moment.duration(this.duration.asMilliseconds() - 1000, 'milliseconds');

      // jw: now we can generate our new state
      const newState = this.calculateState();

      // jw: if the countdown has ended, stop the interval since the UI will not need to keep updating.
      if (newState.hasEnded) {
        this.stop();
      }

      // finally, let's merge the new state into the existing state.
      this.setState(ss => ({ ...ss, ...newState }));
    }, 1000);
  }

  componentWillUnmount() {
    this.stop();
  }

  stop () {
    clearInterval(this.interval);
  }

  render () {
    if (!this.props.children) {
      const { hasEnded } = this.state;

      // jw: if the countdown has ended, let's output a very simple message.
      if (hasEnded) {
        return (
          <TimeWrapper hasEnded={true} inline={this.props.inline}>
            <span>
              <FormattedMessage {...SharedComponentMessages.CountdownEndedText}/>
            </span>
          </TimeWrapper>
        );
      }

      // jw: extract the rest of the variables and render the default UI
      const { hours, days, mins, secs } = this.state;

      // show days if we have a day value
      const showDays = !!(days);
      // always show hours if we are showing days. this way, we still get hour-precision. it will show "3d 0h"
      // which is still useful (vs. just hiding it as previously).
      const showHours = showDays || !!(hours);
      // we'll never show mins if we're showing days. the minute value is pointless if we're days away.
      // we'll also show mins any time there are hours shown so that we can show values like "3h 0m".
      const showMins = !showDays && (showHours || !!(mins));
      // we'll always show seconds as long as we're within an hour of the end. outside of that, seconds are too
      // granular. note also that we'll show "0s" values, whereas we would not have previously.
      const showSecs = !showDays && !showHours;
      const timer = (
        <span>
          {showDays && <span>{days}d&nbsp;&nbsp;</span>}
          {showHours && <span>{hours}h&nbsp;&nbsp;</span>}
          {showMins && <span>{mins}m&nbsp;&nbsp;</span>}
          {showSecs && <span>{secs}s&nbsp;&nbsp;</span>}
        </span>
      );
      const timerContent = this.props.timeOnly ? timer : (
        <React.Fragment>
          <span>
            {timer}
            <span>
              <FormattedMessage {...SharedComponentMessages.CountdownLeftText}/>
            </span>
          </span>
        </React.Fragment>
      );

      return (
        <TimeWrapper hasEnded={false} inline={this.props.inline}>
          {timerContent}
        </TimeWrapper>
      );
    }

    return (
      <React.Fragment>
        {this.props.children(this.state)}
      </React.Fragment>
    );
  }
}
