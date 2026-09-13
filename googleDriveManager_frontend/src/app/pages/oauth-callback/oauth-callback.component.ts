import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-oauth-callback',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="oauth-callback">
      <div class="callback-card">
        @if (success) {
          <div class="status-icon success">✓</div>
          <h2>Google Drive Connected</h2>
          <p>Your Google Drive account has been successfully connected. Redirecting to your drive...</p>
        } @else {
          <div class="status-icon error">✕</div>
          <h2>Connection Failed</h2>
          <p>Something went wrong while connecting your Google Drive account. Please try again.</p>
        }
      </div>
    </div>
  `,
  styles: [`
    .oauth-callback {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #1a237e 0%, #283593 50%, #3949ab 100%);
      padding: 20px;
    }
    .callback-card {
      background: #fff;
      border-radius: 16px;
      padding: 48px 40px;
      max-width: 420px;
      text-align: center;
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    }
    .status-icon {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 32px;
      margin: 0 auto 20px;
      color: #fff;
    }
    .status-icon.success {
      background: #34a853;
    }
    .status-icon.error {
      background: #ea4335;
    }
    h2 {
      font-size: 22px;
      font-weight: 600;
      color: #202124;
      margin: 0 0 12px 0;
    }
    p {
      font-size: 15px;
      color: #5f6368;
      line-height: 1.5;
      margin: 0;
    }
  `]
})
export class OauthCallbackComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  success = false;

  ngOnInit(): void {
    const status = this.route.snapshot.queryParamMap.get('status');
    this.success = status === 'success';
    setTimeout(() => {
      this.router.navigate(['/drive']);
    }, 3000);
  }
}
