import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import styled from 'styled-components';
import { EnhancedPublicationPlanType, PublicationPlanTypeHelper } from 'src/shared/enhancedEnums/publicationPlanType';
import { CreatePublicationMessages } from '../../i18n/CreatePublicationMessages';
import { PageHeader } from '../PageHeader';
import { Link } from '../Link';
import { compose } from 'recompose';
import { Paragraph } from '../Paragraph';
import { PublicationPlanTableRoleRow } from './PublicationPlanTableRoleRow';
import { EnhancedPublicationRole } from '../../enhancedEnums/publicationRole';
import { PublicationRole } from '@narrative/shared';

const PriceTable = styled.table`
  
  background: #FFFFFF;
  border: solid 1px #DDDDDD;
  margin-bottom: 1.25rem;
  
  table-layout: auto;
  width: 100%;
  border-spacing: 2px;
  border-collapse: separate;
  tr:nth-of-type(even) {
    background: #F9F9F9;
  }
  tr td {
    color: #222222;
    font-size: 0.875rem;
    padding: 0.5625rem 0.625rem;
    text-align: left;
    display: table-cell;
    line-height: 1.125rem;
    margin:0;
  }
  
`;

const PublicationPlanTableComponent: React.SFC<{}> = () => {

  // TODO: zb - Add custom domain support below when we decide to allow it, but follow the existing pattern
  /*
  function getPlanCustomDomainSupportedRow()
  {
    return (
      <React.Fragment>
        <td>{<FormattedMessage {...CreatePublicationMessages.PlanTypeCustomDomainSupportedLabel}/>}</td>
        {
          // tslint:disable-next-line no-any
          EnhancedPublicationPlanType.enhancers.map((item: any, i: number) => (
          <React.Fragment key={'customdomain' + i}>
            <td>{item.supportsCustomDomain ?
              <FormattedMessage {...CreatePublicationMessages.PlanTypeYesLabel}/>
              : <FormattedMessage {...CreatePublicationMessages.PlanTypeNoLabel}/>}
            </td>
          </React.Fragment>
        ))}
      </React.Fragment>
    );
  }*/

  return (
    <React.Fragment>
      <PageHeader
        style={{marginBottom: 0}}
        size={'small'}
        center={'title'}
        title={<FormattedMessage {...CreatePublicationMessages.PublicationTypesHeader}/>}
      />
      <PriceTable>
        <tbody>
        <tr>
            <td style={{width: '100%'}}/>
            {EnhancedPublicationPlanType.enhancers.map((item: PublicationPlanTypeHelper, i: number) => (
              <td key={`planRow-${i}`}>{<FormattedMessage {...item.name}/>}</td>
            ))}
        </tr>
        <PublicationPlanTableRoleRow roleType={EnhancedPublicationRole.get(PublicationRole.WRITER)}/>
        <PublicationPlanTableRoleRow roleType={EnhancedPublicationRole.get(PublicationRole.EDITOR)}/>
        {// TODO: zb - put this back in once we add support for custom domains
          /*<tr>
            {getPlanCustomDomainSupportedRow()}
          </tr>*/}
        <tr>
          <td>{<FormattedMessage {...CreatePublicationMessages.PlanTypeAnnualFeeLabel}/>}</td>
          {EnhancedPublicationPlanType.enhancers.map((item: PublicationPlanTypeHelper, i: number) => (
            <td key={`annualFeeRow-${i}`}>${item.annualFee}</td>
          ))}
        </tr>
        </tbody>
      </PriceTable>
      <Paragraph size="small" color="light">
        <FormattedMessage
          {...CreatePublicationMessages.PlanTypeAnnualFeeAdditionalInfoLabel}
          values={{NRVE: <Link.About type="nrve"/>}}
        />
      </Paragraph>
    </React.Fragment>
  );
};

export default compose()(PublicationPlanTableComponent) as React.ComponentClass;
