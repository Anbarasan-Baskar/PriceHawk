import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, RouterModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  isLoggedIn = false;
  username?: string;

  ngOnInit(): void {
    const user = sessionStorage.getItem('auth-user');
    if (user) {
      this.isLoggedIn = true;
      this.username = JSON.parse(user).username;
    }
  }

  logout(): void {
    sessionStorage.clear();
    window.location.reload();
  }
}
