import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { User, CreateUserRequest, UpdateUserRequest } from '../../models/user.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.css'
})
export class UsersComponent implements OnInit {
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private router = inject(Router);

  users: User[] = [];
  loading = false;
  errorMessage = '';

  showCreateModal = false;
  showEditModal = false;
  showDeleteConfirm = false;

  newUser: CreateUserRequest = { username: '', password: '', role: 'TEAM_MEMBER' };
  editingUser: User | null = null;
  editForm: UpdateUserRequest = {};
  deletingUser: User | null = null;

  createdPassword = '';
  showCreatedModal = false;

  get currentUsername() { return this.authService.getUsername(); }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Failed to load users.';
      }
    });
  }

  openCreateModal(): void {
    this.newUser = { username: '', password: '', role: 'TEAM_MEMBER' };
    this.showCreateModal = true;
  }

  createUser(): void {
    this.userService.createUser(this.newUser).subscribe({
      next: (user) => {
        this.showCreateModal = false;
        this.createdPassword = user.password || '';
        this.showCreatedModal = true;
        this.loadUsers();
      },
      error: (err) => {
        this.errorMessage = 'Failed to create user. Username may already exist.';
      }
    });
  }

  openEditModal(user: User): void {
    this.editingUser = user;
    this.editForm = { username: user.username, password: '', role: user.role };
    this.showEditModal = true;
  }

  updateUser(): void {
    if (!this.editingUser) return;
    const form: UpdateUserRequest = {};
    if (this.editForm.username && this.editForm.username !== this.editingUser.username) {
      form.username = this.editForm.username;
    }
    if (this.editForm.password && this.editForm.password.trim()) {
      form.password = this.editForm.password;
    }
    if (this.editForm.role && this.editForm.role !== this.editingUser.role) {
      form.role = this.editForm.role;
    }
    this.userService.updateUser(this.editingUser.id, form).subscribe({
      next: () => {
        this.showEditModal = false;
        this.editingUser = null;
        this.loadUsers();
      },
      error: (err) => {
        this.errorMessage = 'Failed to update user.';
      }
    });
  }

  openDeleteConfirm(user: User): void {
    this.deletingUser = user;
    this.showDeleteConfirm = true;
  }

  deleteUser(): void {
    if (!this.deletingUser) return;
    this.userService.deleteUser(this.deletingUser.id).subscribe({
      next: () => {
        this.showDeleteConfirm = false;
        this.deletingUser = null;
        this.loadUsers();
      },
      error: (err) => {
        this.errorMessage = 'Failed to delete user.';
      }
    });
  }

  closeAllModals(): void {
    this.showCreateModal = false;
    this.showEditModal = false;
    this.showDeleteConfirm = false;
    this.showCreatedModal = false;
    this.editingUser = null;
    this.deletingUser = null;
  }

  logout(): void {
    this.authService.logout();
  }

  goToDrive(): void {
    this.router.navigate(['/drive']);
  }
}
