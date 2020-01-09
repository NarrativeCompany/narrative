import * as yup from 'yup';
import { ObjectSchema } from 'yup';
import { requiredNrveValidator } from './shared/nrveFormUtils';

export interface PostBidOnAuctionFormValues {
  maxNrveBid: string;
}

export const bidOnAuctionValidationSchema: ObjectSchema<PostBidOnAuctionFormValues> =
  yup.object({maxNrveBid: requiredNrveValidator});

export const bidOnAuctionFormUtil = {
  validationSchema: bidOnAuctionValidationSchema
};
