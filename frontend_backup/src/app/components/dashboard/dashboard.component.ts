import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
    products: any[] = [];
    watchlist: any[] = [];

    constructor(private apiService: ApiService) { }

    ngOnInit(): void {
        this.loadProducts();
    }

    loadProducts() {
        this.apiService.getProducts().subscribe({
            next: (data) => {
                this.products = data;
            },
            error: (e) => console.error(e)
        });
    }
}
