import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { ProductTrackingService } from '../../services/product-tracking.service';
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

    constructor(
        private apiService: ApiService,
        private productTrackingService: ProductTrackingService
    ) {}

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
        const isTracked = !product.isTracked;
        this.productTrackingService.setTracked({
            platformId: product.platformId,
            platform: product.platform,
            isTracked
        }).subscribe({
            next: (updatedProduct) => {
                product.isTracked = updatedProduct.isTracked;
            },
            error: (err) => console.error(err)
        });
    }
}
