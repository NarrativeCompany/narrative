import { KycApplicationInput } from '../types';

export const API_URI = '/api';

/**
 * Helper BodySerializer function to allow multipart uploads via mutation.  This works around the lack of "any" support
 * for GraphQL query args (no way to pass a file down to a BodySerializer).  This function works around the issue by
 * currying the file array and then passing the function to the mutation as a BodySerializer on execution.
 *
 * @param files - the file array to be curried and subsequently bound to the multipart upload
 * @param getNameFromFileUid - if a custom name for formData is necessary use this to extract the name from ant
 * design's RcFile type uid property
 */

// tslint:disable-next-line no-any
export const buildMultipartFileArrayBodySerializerFn = (files: File[]) => (_: any, headers: Headers) => {
    // Allow the browser to set the content type based on the form data - manually setting to multipart mangles
    // the request for some reason so lets let the browser set based on the form data.
    headers.delete('Content-Type');
    const formData = new FormData();

    // Add the files
    files.forEach((curFile: File) => {
      formData.append('file', curFile, curFile.name);
    });

    return { body: formData, headers };
  };

export const buildBodySerializerForKycApplicantMutation =
  // tslint:disable-next-line no-any
  (files: File[]) => (data: KycApplicationInput, headers: Headers) => {
    headers.delete('Content-Type');
    const formData = new FormData();

    formData.append('kycApplicantInput', JSON.stringify(data));
    // Add the files
    files.forEach((curFile: File) => {
      // tslint:disable-next-line no-string-literal
      formData.append(curFile['uid'], curFile, curFile.name);
    });

    return { body: formData, headers };
  };
