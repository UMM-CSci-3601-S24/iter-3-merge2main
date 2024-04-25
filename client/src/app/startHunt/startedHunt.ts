import { CompleteHunt } from "../hunts/completeHunt";

export interface StartedHunt {
  _id: string;
  completeHunt: CompleteHunt;
  accessCode: string;
  endDate?: Date;
  submissionIds?: string[];
}
