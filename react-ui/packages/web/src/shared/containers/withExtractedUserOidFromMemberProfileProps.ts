import { withProps } from 'recompose';
import { WithMemberProfileProps } from '../context/MemberProfileContext';

// jw: let's create a little utility to pull the userOid out of the props, since this seems to be a common need
//     for components consuming the WithMemberProfileProps for apollo queries
export const withExtractedUserOidFromMemberProfileProps = withProps((props: WithMemberProfileProps) => {
  const userOid = props.detailsForProfile &&
    props.detailsForProfile.user &&
    props.detailsForProfile.user.oid;

  return { userOid };
});
