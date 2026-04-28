import { ID } from "./common";

export type DropResult =
  | {
      type: "move";
      id: ID;
      targetParentId: ID | null;
    }
  | {
      type: "reorder";
      id: ID;
      parentId: ID | null;
      targetIndex: number;
    };
