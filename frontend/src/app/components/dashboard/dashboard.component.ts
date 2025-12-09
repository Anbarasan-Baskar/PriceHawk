import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
    products: any[] = [];

    constructor(private apiService: ApiService) {}

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

    toggleTrack(product: any) {
        product.isTracked = !product.isTracked;

        this.apiService.updateTrackStatus(product).subscribe({
            next: () => console.log("Tracking Updated"),
            error: (err) => console.error(err)
        });
    }
}
