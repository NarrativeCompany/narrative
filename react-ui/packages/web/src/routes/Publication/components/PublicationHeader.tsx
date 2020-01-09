import * as React from 'react';
import { PublicationDetail } from '@narrative/shared';
import { Heading } from '../../../shared/components/Heading';
import styled from '../../../shared/styled';
import { EnhancedHorizontalAlignment } from '../../../shared/enhancedEnums/horizontalAlignment';
import { themeTypography } from '../../../shared/styled/theme';

interface Props {
  publicationDetail: PublicationDetail;
}

const ConstrainedImage = styled.img`
  // jw: by using max-width here we are telling the browser to scale the image down, but not up. See:
  //     https://www.w3schools.com/css/css_rwd_images.asp
  max-width: 100%;
  height: auto;
`;

export const PublicationHeader: React.SFC<Props> = (props) => {
  const { publicationDetail: { headerImageUrl, publication, headerImageAlignment } } = props;
  const { name } = publication;

  if (headerImageUrl) {
    const alignment = EnhancedHorizontalAlignment.get(headerImageAlignment);

    return (
      <div style={alignment.css}>
        <ConstrainedImage src={headerImageUrl} alt={name}  />
      </div>
    );
  }

  return (
    <Heading size={1} noMargin={true} textAlign="center" color={themeTypography.textColorLight}>
      {name}
    </Heading>
  );
};
