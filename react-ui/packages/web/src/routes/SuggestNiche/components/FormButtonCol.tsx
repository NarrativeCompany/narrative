import * as React from 'react';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { Icon } from 'antd';
import { Link } from '../../../shared/components/Link';
import { Button } from '../../../shared/components/Button';
import { ButtonHTMLType } from 'antd/lib/button/button';

// tslint:disable no-any
interface ParentProps {
  btnText: string | React.ReactNode;
  backBtnText: string | React.ReactNode;
  onBtnClick?: () => void;
  onBackBtnClick: () => any;
  btnDisabled: boolean;
  htmlType?: ButtonHTMLType;
}
// tslint:enable no-any

export const FormButtonCol: React.SFC<ParentProps> = (props) => {
  const { backBtnText, btnText, onBtnClick, onBackBtnClick, btnDisabled, htmlType } = props;

  return (
    <FlexContainer justifyContent="space-between" alignItems="center">
      <Link.Anchor onClick={onBackBtnClick}>
        <Icon type="left"/> {backBtnText}
      </Link.Anchor>

      <Button
        style={{minWidth: '180px'}}
        size="large"
        type="primary"
        onClick={onBtnClick}
        disabled={btnDisabled}
        htmlType={htmlType}
      >
        {btnText}
      </Button>
    </FlexContainer>
  );
};
