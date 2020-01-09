import * as React from 'react';
import { compose } from 'recompose';
import {
  PublicationDetailsConnect,
  WithPublicationDetailsContextProps
} from '../../../components/PublicationDetailsContext';
import { SEO } from '../../../../../shared/components/SEO';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { PublicationInfoBody } from './PublicationInfoBody';

/*
  jw: Keeping this component simple so that we setup the SEO title prior to making the call to the server for the prfile
      details.
 */
const PublicationInfoComponent: React.SFC<WithPublicationDetailsContextProps> = (props) => {
  return (
    <React.Fragment>
      <SEO
        title={PublicationDetailsMessages.ProfileSeoTitle}
        publication={props.publicationDetail.publication}
      />

      <PublicationInfoBody {...props}/>

    </React.Fragment>
  );
};

export default compose(
  PublicationDetailsConnect
)(PublicationInfoComponent) as React.ComponentClass<{}>;
