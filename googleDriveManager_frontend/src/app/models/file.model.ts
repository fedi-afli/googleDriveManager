export interface FileItem {
  id: string;
  name: string;
  mimeType: string;
  folder: boolean;
  parents: string[];
}

export interface MoveFileRequest {
  targetFolderId: string;
}

export interface RenameFileRequest {
  newName: string;
}
