import * as React from 'react';
import { compose } from 'recompose';
import {
  withPermissionsModalHelpers, WithPermissionsModalHelpersProps
} from '../../../shared/containers/withPermissionsModalHelpers';
import { RevokeReasonMessages } from '../../../shared/i18n/RevokeReasonMessages';
import { AuthorRateContentWarningProps } from '../../../shared/components/rating/AuthorRateContentWarning';
import { Modal } from 'antd';
import { FormattedMessage } from 'react-intl';
import { Post } from '@narrative/shared';
import { PostLink } from '../../../shared/components/post/PostLink';
import { RatingMessages } from '../../../shared/i18n/RatingMessages';
import { PermissionErrorModal } from '../../../shared/components/PermissionErrorModal';

interface ParentProps extends AuthorRateContentWarningProps {
  post: Post;
}

type Props =
  ParentProps &
  WithPermissionsModalHelpersProps;

const AuthorAgeRatePostWarningComponent: React.SFC<Props> = (props) => {
  const { post, visible, dismiss, permissionErrorModalProps, permissionLinkSecurer } = props;

  const editLink = (
    <PostLink post={post} isEditLink={true} linkSecurer={permissionLinkSecurer}>
      <FormattedMessage {...RatingMessages.Edit}/>
    </PostLink>
  );

  return (
    <React.Fragment>
      <Modal
        visible={visible}
        footer={null}
        onCancel={dismiss}
      >
        <FormattedMessage {...RatingMessages.AuthorAgeRateWarning} values={{editLink}}/>
      </Modal>
      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps}/>}
    </React.Fragment>
  );
};

export const AuthorAgeRatePostWarning = compose(
  withPermissionsModalHelpers('postContent', RevokeReasonMessages.EditPosts)
)(AuthorAgeRatePostWarningComponent) as React.ComponentClass<ParentProps>;
