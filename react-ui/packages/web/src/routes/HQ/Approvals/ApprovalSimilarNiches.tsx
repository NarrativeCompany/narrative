import * as React from 'react';
import { compose, withProps } from 'recompose';
import { Tooltip } from 'antd';
import { Heading } from '../../../shared/components/Heading';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { Niche, withSimilarNichesByNicheId, WithSimilarNichesByNicheIdProps } from '@narrative/shared';
import { ApprovalSimilarNichesMessages } from '../../../shared/i18n/ApprovalSimilarNichesMessages';
import { FormattedMessage } from 'react-intl';
import { NicheLink } from '../../../shared/components/niche/NicheLink';
import styled from '../../../shared/styled';
import { SidebarMessages } from '../../../shared/i18n/SidebarMessages';

const SimilarNichesBlock = styled<FlexContainerProps>(FlexContainer)`
  width: 100%;
  text-align: center;
  color: ${props => props.theme.textColorLight};
  border-top: 1px solid ${props => props.theme.borderGrey};
  height: 40px;
  cursor: default;
`;

const TooltipContent = styled<FlexContainerProps>(FlexContainer)`
  padding: 10px;
  width: 100%;
  
  a {
    color: #fff;
    word-break: normal;
    white-space: nowrap;
    margin-right: 8px;
    text-decoration: underline;
  }
  
  p {
    color: #fff;
    word-break: normal;
    white-space: nowrap;
    margin-right: 8px;
  }
`;

const SimilarNichesWrapper = styled<FlexContainerProps>(FlexContainer)`
  width: 100%;
  flex-wrap: wrap;
`;

interface ParentProps {
  nicheId: string;
}

interface WithProps {
  similarNiches: Niche[];
}

type Props =
  ParentProps &
  WithProps;

const ApprovalSimilarNichesComponent: React.SFC<Props> = (props) => {
  const { similarNiches } = props;
  const nichesLength = similarNiches.length;

  const btnText = nichesLength ?
      <FormattedMessage values={{ nichesLength }} {...ApprovalSimilarNichesMessages.btnTextHasSimilarNiches}/> :
      <FormattedMessage {...ApprovalSimilarNichesMessages.btnTextNoSimilarNiches}/>;

  if (!nichesLength) {
    return <SimilarNichesBlock centerAll={true}>{btnText}</SimilarNichesBlock>;
  }

  const title = (
    <TooltipContent column={true}>
      <Heading uppercase={true} size={6} color="#fff">
        <FormattedMessage {...SidebarMessages.SimilarNiches} />
      </Heading>

      <SimilarNichesWrapper>
        {similarNiches.map(niche => (
          <NicheLink key={niche.oid} niche={niche}/>
        ))}
      </SimilarNichesWrapper>
    </TooltipContent>
  );

  return (
    <Tooltip placement="bottomLeft" title={title} overlayStyle={{ maxWidth: 325 }}>
      <SimilarNichesBlock centerAll={true}>{btnText}</SimilarNichesBlock>
    </Tooltip>
  );
};

export const ApprovalSimilarNiches = compose(
  withSimilarNichesByNicheId,
  withProps((props: WithSimilarNichesByNicheIdProps) => {
    const { similarNichesByNicheIdData } = props;

    const similarNiches =
      similarNichesByNicheIdData &&
      similarNichesByNicheIdData.getSimilarNichesByNicheId || [];

    return { similarNiches };
  })
)(ApprovalSimilarNichesComponent) as React.ComponentClass<ParentProps>;
