import * as yup from 'yup';

export const fileUploadValidator = yup
  .object({
    tempFile: yup.object({
      oid: yup.string().required(),
      token: yup.string().required()
    // jw: since this object is optional lets default it to null, and then flag it as nullable. Otherwise, since it has
    //     required children it will be treated as a empty object and result in a false error.
    }).default(null).nullable(),
    remove: yup.boolean()
  })
  .required();
