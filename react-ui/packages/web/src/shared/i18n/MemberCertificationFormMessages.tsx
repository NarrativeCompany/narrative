import { defineMessages } from 'react-intl';

export const MemberCertificationFormMessages = defineMessages({
  PageHeaderTitle: {
    id: 'memberCertificationFormMessages.pageHeaderTitle',
    defaultMessage: 'Member Certification'
  },
  SelectPlaceholder: {
    id: 'memberCertificationFormMessages.selectPlaceholder',
    defaultMessage: 'Choose One'
  },
  IdTypeSelectionLabel: {
    id: 'memberCertificationFormMessages.idTypeSelectionLabel',
    defaultMessage: '1. Choose ID Type'
  },
  IdTypeSelectionHelperTextOne: {
    id: 'memberCertificationFormMessages.idTypeSelectionHelperTextOne',
    defaultMessage: 'Select a valid form of identification. Be sure the image is clear and not blurry so that we' +
      ' can ensure your information is correct.'
  },
  IdTypeSelectionFrontLabel: {
    id: 'memberCertificationFormMessages.idTypeSelectionFrontLabel',
    defaultMessage: `Front of {idType}`
  },
  IdTypeSelectionBackLabel: {
    id: 'memberCertificationFormMessages.idTypeSelectionBackLabel',
    defaultMessage: `Back of {idType}`
  },
  IdTypeDriversLicense: {
    id: 'memberCertificationFormMessages.idTypeDriversLicense',
    defaultMessage: `Driver's License`
  },
  IdTypePassport: {
    id: 'memberCertificationFormMessages.idTypePassport',
    defaultMessage: 'Passport'
  },
  IdTypeGovernmentId: {
    id: 'memberCertificationFormMessages.idTypeGovernmentId',
    defaultMessage: 'Government ID'
  },
  UserImageUploadLabel: {
    id: 'memberCertificationFormMessages.userImageUploadLabel',
    defaultMessage: `2. Upload an Image of Yourself and Today's Date`
  },
  UserImageUploadHelperText: {
    id: 'memberCertificationFormMessages.userImageUploadHelperText',
    defaultMessage: 'Please upload a clear image of yourself holding a paper that reads {boldOne} and {boldTwo}.'
  },
  UserImageUploadHelperTextBoldOne: {
    id: 'memberCertificationFormMessages.userImageUploadHelperTextBoldOne',
    defaultMessage: '"Narrative Certification"'
  },
  UserImageUploadHelperTextBoldTwo: {
    id: 'memberCertificationFormMessages.userImageUploadHelperTextBoldTwo',
    defaultMessage: 'the date of submission'
  },
  UserImageDragLabel: {
    id: 'memberCertificationFormMessages.userImageDragLabel',
    defaultMessage: 'Selfie and sign'
  },
  FileSizeTooLargeErrorMsg: {
    id: 'memberCertificationFormMessages.fileSizeTooLargeErrorMsg',
    defaultMessage: 'File size must be smaller than 10MB'
  }
});
