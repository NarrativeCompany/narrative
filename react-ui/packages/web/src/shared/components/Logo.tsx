import * as React from 'react';
import { compose } from 'recompose';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import { Link } from './Link';
import { WebRoute } from '../constants/routes';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import primaryLogo from '../../assets/narrative-logo.svg';
import secondaryLogo from '../../assets/narrative-logo-blue.svg';
import styled from '../../shared/styled';

const LogoWrapper = styled<{type?: LogoType}, 'div'>('div')`

  ${props => props.type && props.type === 'secondary' ? `
      position: relative;
      top: -10px;
      left: -10px;
    ` : `
      max-width: 25px;
      transition: all .15s ease-in-out;
    
      @media only screen and (min-width: 767px) {
      max-width: 32px;
      transition: all .15s ease-in-out;
    `
  };
`;

const LogoImg = styled<{type?: LogoType}, 'img'>('img')`
  ${props => props.type && props.type === 'primary' && `
    width: 100%;
    min-width: 25px;
    transition: all .15s ease-in-out;
  `};
`;

const logos = {
  primary: primaryLogo,
  secondary: secondaryLogo,
};

type LogoType = 'primary' | 'secondary';

interface LogoProps {
  type?: LogoType;
  isLink?: boolean;
}

type Props =
  LogoProps &
  InjectedIntlProps;

export const LogoComponent: React.SFC<Props> = (props) => {
  const { type, isLink, intl: { formatMessage } } = props;

  const Content = (
    <LogoWrapper type={type}>
      <LogoImg
        type={type}
        src={type ? logos[type] : logos.primary}
        alt={formatMessage(SharedComponentMessages.Narrative)}
      />
    </LogoWrapper>
  );

  if (!isLink) {
    return Content;
  }

  return <Link to={WebRoute.Home} className="narrative-logo">{Content}</Link>;
};

export const Logo = compose(
  injectIntl
)(LogoComponent) as React.ComponentClass<LogoProps>;
