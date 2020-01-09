import { Niche, Referendum, User } from '@narrative/shared';

export function getNicheFromReferendum(referendum: Referendum): Niche | null {
  return referendum &&
    referendum.niche;
}

export function getSuggesterFromReferendum(referendum: Referendum): User | null {
  return referendum && referendum.niche && referendum.niche.suggester;
}
