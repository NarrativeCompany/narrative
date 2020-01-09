import { TribunalIssueType } from '@narrative/shared';

export function getAvailableTribunalIssueTypeByType(
  availableTribunalIssues: TribunalIssueType[],
  tribunalIssueType: TribunalIssueType
): TribunalIssueType | null {
  if (!availableTribunalIssues || !availableTribunalIssues.length) {
    return null;
  }

  return availableTribunalIssues
    .find((type: TribunalIssueType) => type === tribunalIssueType) || null;
}

export function canCurrentUserSubmitTribunalAppeal (availableTribunalIssues: TribunalIssueType[]): boolean {
  return availableTribunalIssues
    .some((type: TribunalIssueType) =>
      type === TribunalIssueType.APPROVE_REJECTED_NICHE ||
      type === TribunalIssueType.RATIFY_NICHE
    );
}
