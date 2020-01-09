import { Component } from 'react';
import { RouteComponentProps, StaticContext, withRouter } from 'react-router';
import { scrollToTop } from '../utils/scrollUtils';

// zb: you can now avoid the page scrolling to the top when pushing history by setting a state parameter
// example: props.history.push(newURL, {scrollToTop: false});
class ScrollToTopComponent extends Component<RouteComponentProps<{}>, StaticContext> {
  componentDidUpdate(prevProps: RouteComponentProps<{}>) {
    if (this.props.location !== prevProps.location
      && (!this.props.location.state || this.props.location.state.scrollToTop !== false)
    ) {
      // jw: if anything is specified in the hash then we are likely trying to link to a element on the page. Short out
      //     so that `UrlFragmentAnchor` can resolve this.
      const elementId = location.hash.slice(1);
      if (elementId) {
        return;
      }

      // jw: otherwise, scroll to the top of the screen.
      scrollToTop();
    }
  }

  render() {
    return this.props.children;
  }
}

export const ScrollToTop = withRouter(ScrollToTopComponent);
