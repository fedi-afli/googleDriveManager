import { Routes } from '@angular/router';
import { authGuard, managerGuard } from './guards/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { DriveComponent } from './pages/drive/drive.component';
import { UsersComponent } from './pages/users/users.component';
import { OauthCallbackComponent } from './pages/oauth-callback/oauth-callback.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'drive', component: DriveComponent, canActivate: [authGuard] },
  { path: 'users', component: UsersComponent, canActivate: [authGuard, managerGuard] },
  { path: 'oauth-callback', component: OauthCallbackComponent },
  { path: '**', redirectTo: '/login' }
];
