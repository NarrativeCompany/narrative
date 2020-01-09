import * as React from 'react';
import { compose } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { SEO } from '../../../shared/components/SEO';
import { SidebarViewWrapper } from '../../../shared/components/SidebarViewWrapper';
import { PageHeader } from '../../../shared/components/PageHeader';

interface ParentProps {
  title: FormattedMessage.MessageDescriptor;
  description?: FormattedMessage.MessageDescriptor;
  seoTitle?: FormattedMessage.MessageDescriptor;
  seoDescription?: FormattedMessage.MessageDescriptor;
  omitSeoSuffix?: boolean;

  headerContent?: React.ReactNode;

  // tslint:disable-next-line no-any
  sidebarItems?: React.ReactElement<any>;
}

type Props = ParentProps;

const HomeWrapperComponent: React.SFC<Props> = (props) => {
  const {
    seoTitle,
    seoDescription,
    omitSeoSuffix,
    title,
    description,
    headerContent,
    sidebarItems
  } = props;

  return (
    <React.Fragment>
      <SEO
        title={seoTitle || title}
        description={seoDescription}
        omitSuffix={omitSeoSuffix}
      />

      <SidebarViewWrapper
        sidebarItems={sidebarItems}
        headerContent={
          <React.Fragment>
            <PageHeader
              title={<FormattedMessage {...title}/>}
              description={description && <FormattedMessage {...description}/>}
            />
            {headerContent}
          </React.Fragment>
        }
      >
        {props.children}

      </SidebarViewWrapper>
    </React.Fragment>
  );
};

export const HomeWrapper = compose(
)(HomeWrapperComponent) as React.ComponentClass<ParentProps>;
