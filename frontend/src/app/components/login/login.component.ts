import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  form: any = {
    email: null,
    password: null
  };
  isLoggedIn = false;
  isLoginFailed = false;
  errorMessage = '';

  constructor(private api: ApiService, private router: Router) { }

  onSubmit(): void {
    const { email, password } = this.form;
    this.api.login({ email, password }).subscribe({
      next: data => {
        sessionStorage.setItem('auth-token', data.accessToken);
        sessionStorage.setItem('auth-user', JSON.stringify(data));
        this.isLoginFailed = false;
        this.isLoggedIn = true;
        this.router.navigate(['/']);
      },
      error: err => {
        this.errorMessage = err.error.message;
        this.isLoginFailed = true;
      }
    });
  }
}
