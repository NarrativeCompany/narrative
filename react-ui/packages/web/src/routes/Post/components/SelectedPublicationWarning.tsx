import * as React from 'react';
import { compose } from 'recompose';
import {
  withPublicationProfile,
  WithPublicationProfileParentProps,
  WithPublicationProfileProps
} from '@narrative/shared';
import { withLoadingPlaceholder } from '../../../shared/utils/withLoadingPlaceholder';
import {
  EnhancedPublicationContentRewardWriterShare
} from '../../../shared/enhancedEnums/publicationContentRewardWriterShare';
import styled from '../../../shared/styled';
import { CardProps } from 'antd/lib/card';
import { Card } from 'antd';
import { FormattedMessage } from 'react-intl';
import { PostMessages } from '../../../shared/i18n/PostMessages';
import { Block } from '../../../shared/components/Block';
import { Paragraph } from '../../../shared/components/Paragraph';

const PublicationWarningCard = styled<CardProps>(Card)`
  &.ant-card {
    margin-bottom: 24px;
    
    .ant-card-body {
      padding: 12px 16px;
    }  
  }
`;

const SelectedPublicationWarningComponent: React.SFC<WithPublicationProfileProps> = (props) => {
  const { publicationProfile: { contentRewardWriterShare } } = props;

  const writerShare = EnhancedPublicationContentRewardWriterShare.get(contentRewardWriterShare);
  const publicationPercentage = writerShare.getPublicationPercentageString();

  return (
    <PublicationWarningCard>
      <Block color="warning">
        <Paragraph marginBottom="large" color="inherit">
          <FormattedMessage {...PostMessages.PublicationWarning}/>
        </Paragraph>
        <ul>
          <li><FormattedMessage {...PostMessages.PublicationWarningPointOne}/></li>
          <li><FormattedMessage {...PostMessages.PublicationWarningPointTwo} values={{publicationPercentage}}/></li>
        </ul>
      </Block>
    </PublicationWarningCard>
  );
};

export const SelectedPublicationWarning = compose(
  withPublicationProfile,
  withLoadingPlaceholder()
)(SelectedPublicationWarningComponent) as React.ComponentClass<WithPublicationProfileParentProps>;
