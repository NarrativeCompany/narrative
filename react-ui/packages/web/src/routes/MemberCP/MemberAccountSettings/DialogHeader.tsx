import * as React from 'react';
import { Paragraph } from '../../../shared/components/Paragraph';
import { Divider } from 'antd';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { FormMethodError } from '../../../shared/components/FormMethodError';
import { Heading } from '../../../shared/components/Heading';
import { MethodError } from '@narrative/shared';

interface ParentProps extends MethodError {
  icon?: React.ReactNode;
  title: React.ReactNode;
  description: React.ReactNode;
  showDivider?: boolean | false;
  includeFormMethodError?: boolean | false;
}

export const DialogHeader: React.SFC<ParentProps> = (props) => {
  const {icon, title, description, showDivider, includeFormMethodError, methodError} = props;

  const Title =
    typeof title === 'string' ?
    <Heading size={3}>{title}</Heading> :
    <React.Fragment>{title}</React.Fragment>;

  return (
    <React.Fragment>

      <FlexContainer column={true} centerAll={true}>

        {icon && icon}

        {Title}

        <Paragraph textAlign="center" marginBottom="large">
          {description}
        </Paragraph>

        {includeFormMethodError &&
          <FormMethodError methodError={methodError || null}/>
        }

        {showDivider &&
          <Divider/>
        }

      </FlexContainer>

    </React.Fragment>
  );
};
