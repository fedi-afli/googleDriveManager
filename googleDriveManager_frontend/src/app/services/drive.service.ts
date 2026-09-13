import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FileItem, MoveFileRequest, RenameFileRequest } from '../models/file.model';

@Injectable({
  providedIn: 'root'
})
export class DriveService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/drive';

  listFiles(folderId: string | null): Observable<FileItem[]> {
    const params: any = {};
    if (folderId) params.folderId = folderId;
    return this.http.get<FileItem[]>(`${this.apiUrl}/files`, { params });
  }

  uploadFile(file: File, folderId: string | null, customName: string | null): Observable<FileItem> {
    const formData = new FormData();
    formData.append('file', file);
    if (folderId) formData.append('folderId', folderId);
    if (customName) formData.append('customName', customName);
    return this.http.post<FileItem>(`${this.apiUrl}/upload`, formData);
  }

  moveFile(fileId: string, request: MoveFileRequest): Observable<FileItem> {
    return this.http.put<FileItem>(`${this.apiUrl}/files/${fileId}/move`, request);
  }

  renameFile(fileId: string, request: RenameFileRequest): Observable<FileItem> {
    return this.http.put<FileItem>(`${this.apiUrl}/files/${fileId}/rename`, request);
  }

  deleteFile(fileId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/files/${fileId}`);
  }
}
