import { createUrl } from './routeUtils';

export function scrollToTop () {
  if (!window.pageYOffset) {
    return;
  }

  // jw: we need to allow the current dom block to render before we trigger the scroll to top. What this does is allows
  //     the browser to do its 'auto' scrollRestoration before we force the viewport to the top. Then whatever other
  //     content gets loaded into the page will happen after, with no ill effect from the browser.
  setTimeout(() => {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  });
}

// jw: utility to scroll to a element by ID, if the element is not found after the current route resolves then we will
//     revert to using scrollToTop above.
export function scrollToUrlHashElement(elementId: string) {
  const fragment = location.hash.slice(1);

  // jw: if the url fragment does not match the elementId then there is nothing to do, short out.
  if (elementId !== fragment) {
    return;
  }

  scrollToElement(elementId);
}

export function scrollToElement(elementId: string) {
  // jw: we want to allow the current HTML to finish writing to the body before we scroll, so let's create a timeout
  //     with no delay. That should trigger this once the HTML is ready.
  setTimeout(() => {
    const element = document.getElementById(elementId);

    if (!element) {
      // todo:error-handling: We should always have an element since this is only called from places where we added it.
      return;
    }

    // zb: strip out the fragment so we don't attempt to scroll on future renders
    // we are using window.history instead of the react history component specifically
    // because we don't want to cause a re-route.
    // It is also VITAL for this to happen as part of the setTimeout so that it does not
    // remove the fragment from the URL prior to ScrollToTop.componentDidUpdate
    const newURL = createUrl(location.href, { fragment: undefined });
    history.pushState(null, '', newURL);

    // jw: guess we need to figure out where to scroll to
    const bodyRect = document.body.getBoundingClientRect();
    const elementRect = element.getBoundingClientRect();

    // jw: finally, perform the scroll.
    window.scrollTo({
      top: elementRect.top - bodyRect.top,
      behavior: 'smooth'
    });
  });
}
