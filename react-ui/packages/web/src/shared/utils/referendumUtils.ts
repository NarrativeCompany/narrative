import { Referendum } from '@narrative/shared';

export function getApprovalPercentage(rawVotePointsFor: string, rawVotePointsAgainst: string): number {
  const votePointsFor: number = parseFloat(rawVotePointsFor);
  const votePointsAgainst: number = parseFloat(rawVotePointsAgainst);

  const totalVotePoints = votePointsFor + votePointsAgainst;

  if (!totalVotePoints) {
    return 0;
  }

  const diff = (votePointsFor / totalVotePoints) * 100;

  return parseFloat(diff.toFixed(2));
}

export function wasReferendumPassed(referendum: Referendum): boolean {
  if (referendum.open) {
    // todo:error-handling: log to the server, we should never get here for a open referendum!
    return false;
  }

  // jw: we always require a simple majority, with that in mind, let's check that there are more votes for than against.
  return parseFloat(referendum.votePointsFor) > parseFloat(referendum.votePointsAgainst);
}
