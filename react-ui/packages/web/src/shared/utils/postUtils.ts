import { generatePath } from 'react-router';
import { WebRoute } from '../constants/routes';

export function getPostUrl (prettyUrlString: string | null, postOid: string): string {
  let url: string;

  if (prettyUrlString) {
    url = generatePath(WebRoute.PostDetails, {id: prettyUrlString});
  } else {
    const postURLOid = '_' + postOid;
    url = generatePath(WebRoute.PostDetails, {id: postURLOid});
  }

  return url;
}
