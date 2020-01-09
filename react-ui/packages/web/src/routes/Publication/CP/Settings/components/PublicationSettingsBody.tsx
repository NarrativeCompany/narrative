import * as React from 'react';
import { compose, withProps } from 'recompose';
import { Publication, withPublicationSettings, WithPublicationSettingsProps } from '@narrative/shared';
import { fullPlaceholder, withLoadingPlaceholder } from '../../../../../shared/utils/withLoadingPlaceholder';
import { PublicationSettingsForm } from './PublicationSettingsForm';

interface ParentProps {
  publication: Publication;
}

const PublicationSettingsBodyComponent: React.SFC<WithPublicationSettingsProps> = (props) => {
  const { publicationSettings } = props;

  // jw: The key here seems odd, but because form updates will cause a new PublicationSettings object to be created we
  //     need to ensure that anytime a render is triggered here the form will re-render from the fresh data. This is
  //     particularly important for the images, which after application should no longer appear as a uploaded image, but
  //     the current image for the Publication. Without this, the images will appear as uploaded, and cause an error the
  //     next time the form is submitted because it will be trying to use the temporary file data for a file that has
  //     already been applied.
  return <PublicationSettingsForm key={new Date().getTime()} publicationSettings={publicationSettings} />;
};

export const PublicationSettingsBody = compose(
  withProps((props: ParentProps) => {
    const publicationOid = props.publication.oid;

    return { publicationOid };
  }),
  withPublicationSettings,
  withLoadingPlaceholder(fullPlaceholder)
)(PublicationSettingsBodyComponent) as React.ComponentClass<ParentProps>;
