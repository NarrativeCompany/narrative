import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { SectionHeader } from '../SectionHeader';
import { SidebarMessages } from '../../i18n/SidebarMessages';
import { Block } from '../Block';
import { Link } from '../Link';

export const PublicationsPromoSidebarItem: React.SFC<{}> = () => {
  const publicationsLink = <Link.About type="publications"/>;

  return (
    <Block style={{marginBottom: 30}}>
      <SectionHeader
        title={<FormattedMessage {...SidebarMessages.Publications} />}
        description={<FormattedMessage {...SidebarMessages.PublicationsDescription} values={{publicationsLink}} />}
      />
    </Block>
  );
};
