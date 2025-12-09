import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ProductComparisonComponent } from './components/product-comparison/product-comparison.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';

export const routes: Routes = [
    { path: '', component: DashboardComponent },
    { path: 'compare', component: ProductComparisonComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent }
];
