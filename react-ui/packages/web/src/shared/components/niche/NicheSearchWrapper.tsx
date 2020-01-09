import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { PageHeader, PageHeaderCenterType } from '../PageHeader';
import { Link } from '../Link';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { WebRoute } from '../../constants/routes';

interface NicheSearchWrapperProps {
  title: FormattedMessage.MessageDescriptor;
  description: FormattedMessage.MessageDescriptor;
  cancelLink?: FormattedMessage.MessageDescriptor;
  centerType?: PageHeaderCenterType;
}

export const NicheSearchWrapper: React.SFC<NicheSearchWrapperProps> = (props) => {
  const { title, description, centerType, cancelLink, children } = props;

  const learnMoreLink = (
    <Link.About type="niche">
      <FormattedMessage {...SharedComponentMessages.LearnAboutNichesLink}/>
    </Link.About>
  );

  const extra = cancelLink && <Link to={WebRoute.Home} color="light"><FormattedMessage {...cancelLink}/></Link>;

  return (
    <div style={{maxWidth: 880, minHeight: 'auto'}}>
      <PageHeader
        title={<FormattedMessage {...title}/>}
        description={<FormattedMessage {...description} values={{ learnMoreLink }}/>}
        extra={extra}
        center={centerType}
        size={2}
        style={{ marginBottom: 40 }}
      />

      {children}
    </div>
  );
};
