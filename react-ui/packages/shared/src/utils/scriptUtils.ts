import {
  lifecycle,
} from 'recompose';

export function withIncludeScript (src: string) {
  return lifecycle({
    componentDidMount() {
      includeScript(src);
    }
  });
}

export function includeScript (
  src: string,
  onLoad?: () => void,
  modifyScript?: (script: HTMLScriptElement) => void,
  async?: boolean
): void {
  const result = document.querySelectorAll('script[src="' + src + '"]');

  if (result && result.length === 0) {
    const aScript = document.createElement('script');
    aScript.type = 'text/javascript';
    aScript.src = src;
    if (modifyScript) {
      modifyScript(aScript);
    }
    if (async) {
      aScript.async = true;
    }
    if (onLoad) {
      aScript.onload = () => {
        onLoad();
      };
    }
    if (document.head) {
      document.head.appendChild(aScript);
    }
  } else if (onLoad) {
    onLoad();
  }
}
