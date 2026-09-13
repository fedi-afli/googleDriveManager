import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { DriveService } from '../../services/drive.service';
import { FileItem } from '../../models/file.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-drive',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './drive.component.html',
  styleUrl: './drive.component.css'
})
export class DriveComponent implements OnInit {
  private driveService = inject(DriveService);
  private authService = inject(AuthService);
  private router = inject(Router);

  files: FileItem[] = [];
  folders: FileItem[] = [];
  currentFolderId: string | null = null;
  breadcrumbs: { id: string | null; name: string }[] = [{ id: null, name: 'My Drive' }];
  loading = false;
  errorMessage = '';

  selectedFile: FileItem | null = null;
  showMoveModal = false;
  showRenameModal = false;
  showUploadModal = false;
  showDeleteConfirm = false;

  newName = '';
  selectedFileUpload: File | null = null;
  uploadCustomName = '';
  uploading = false;

  get username() { return this.authService.getUsername(); }
  get isManager() { return this.authService.isManager(); }

  ngOnInit(): void {
    this.loadFiles();
  }

  loadFiles(): void {
    this.loading = true;
    this.errorMessage = '';
    this.driveService.listFiles(this.currentFolderId).subscribe({
      next: (items) => {
        this.folders = items.filter(f => f.folder);
        this.files = items.filter(f => !f.folder);
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Failed to load files. Please try again.';
      }
    });
  }

  openFolder(folder: FileItem): void {
    this.currentFolderId = folder.id;
    this.breadcrumbs.push({ id: folder.id, name: folder.name });
    this.loadFiles();
  }

  navigateToBreadcrumb(index: number): void {
    this.breadcrumbs = this.breadcrumbs.slice(0, index + 1);
    this.currentFolderId = this.breadcrumbs[this.breadcrumbs.length - 1].id;
    this.loadFiles();
  }

  openUploadModal(): void {
    this.selectedFileUpload = null;
    this.uploadCustomName = '';
    this.showUploadModal = true;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFileUpload = input.files[0];
      if (!this.uploadCustomName) {
        this.uploadCustomName = this.selectedFileUpload.name;
      }
    }
  }

  confirmUpload(): void {
    if (!this.selectedFileUpload) return;
    this.uploading = true;
    this.driveService.uploadFile(
      this.selectedFileUpload,
      this.currentFolderId,
      this.uploadCustomName || null
    ).subscribe({
      next: () => {
        this.uploading = false;
        this.showUploadModal = false;
        this.loadFiles();
      },
      error: (err) => {
        this.uploading = false;
        this.errorMessage = 'Upload failed. Please try again.';
      }
    });
  }

  openMoveModal(file: FileItem): void {
    this.selectedFile = file;
    this.showMoveModal = true;
  }

  moveFile(targetFolderId: string): void {
    if (!this.selectedFile) return;
    this.driveService.moveFile(this.selectedFile.id, { targetFolderId }).subscribe({
      next: () => {
        this.showMoveModal = false;
        this.selectedFile = null;
        this.loadFiles();
      },
      error: (err) => {
        this.errorMessage = 'Failed to move file. Please try again.';
      }
    });
  }

  openRenameModal(file: FileItem): void {
    this.selectedFile = file;
    this.newName = file.name;
    this.showRenameModal = true;
  }

  confirmRename(): void {
    if (!this.selectedFile || !this.newName.trim()) return;
    this.driveService.renameFile(this.selectedFile.id, { newName: this.newName.trim() }).subscribe({
      next: () => {
        this.showRenameModal = false;
        this.selectedFile = null;
        this.loadFiles();
      },
      error: (err) => {
        this.errorMessage = 'Failed to rename file. Please try again.';
      }
    });
  }

  openDeleteConfirm(file: FileItem): void {
    this.selectedFile = file;
    this.showDeleteConfirm = true;
  }

  confirmDelete(): void {
    if (!this.selectedFile) return;
    this.driveService.deleteFile(this.selectedFile.id).subscribe({
      next: () => {
        this.showDeleteConfirm = false;
        this.selectedFile = null;
        this.loadFiles();
      },
      error: (err) => {
        this.errorMessage = 'Failed to delete file. Please try again.';
      }
    });
  }

  closeAllModals(): void {
    this.showMoveModal = false;
    this.showRenameModal = false;
    this.showUploadModal = false;
    this.showDeleteConfirm = false;
    this.selectedFile = null;
  }

  logout(): void {
    this.authService.logout();
  }

  goToUserManagement(): void {
    this.router.navigate(['/users']);
  }

  getFileIcon(mimeType: string): string {
    if (mimeType.includes('image')) return '🖼️';
    if (mimeType.includes('pdf')) return '📄';
    if (mimeType.includes('video')) return '🎬';
    if (mimeType.includes('audio')) return '🎵';
    if (mimeType.includes('zip') || mimeType.includes('compressed')) return '🗜️';
    if (mimeType.includes('spreadsheet')) return '📊';
    if (mimeType.includes('document') || mimeType.includes('word')) return '📝';
    if (mimeType.includes('presentation')) return '📽️';
    return '📄';
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }
}
