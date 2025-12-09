import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { CommonModule } from '@angular/common';
import { Chart } from 'chart.js/auto';

@Component({
    selector: 'app-product-comparison',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div *ngIf="product" class="comparison-container">
      <h2>{{ product.name }}</h2>
      
      <div class="deal-section">
        <div class="current-deal">
            <h3>Current: {{ product.platform }}</h3>
            <p class="price">₹{{ product.currentPrice }}</p>
        </div>
        
        <div class="best-deal" *ngIf="bestDeal">
            <h3>Best Deal: {{ bestDeal.platform }}</h3>
            <p class="price highlight">₹{{ bestDeal.currentPrice }}</p>
            <p *ngIf="bestDeal.id !== product.id">Save ₹{{ product.currentPrice - bestDeal.currentPrice }}!</p>
        </div>
      </div>

      <div class="chart-container">
        <canvas id="priceHistoryChart"></canvas>
      </div>
      
      <div class="ai-prediction">
        <h3>AI Prediction</h3>
        <!-- Prediction content would go here -->
        <p>Confidence: High</p>
      </div>
    </div>
  `,
    styles: [`
    .comparison-container { padding: 20px; }
    .deal-section { display: flex; gap: 20px; margin-bottom: 20px; }
    .highlight { color: green; font-weight: bold; }
    .chart-container { height: 300px; width: 100%; }
  `]
})
export class ProductComparisonComponent implements OnInit {
    product: any;
    bestDeal: any;
    chart: any;

    constructor(
        private route: ActivatedRoute,
        private apiService: ApiService
    ) { }

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        this.loadProduct(id);
        this.loadHistory(id);
    }

    loadProduct(id: any) {
        this.apiService.getProduct(id).subscribe(data => {
            this.product = data;
            this.apiService.getBestDeal(id).subscribe(deal => {
                this.bestDeal = deal;
            });
        });
    }

    loadHistory(id: any) {
        this.apiService.getPriceHistory(id).subscribe(history => {
            this.initChart(history);
        });
    }

    initChart(history: any[]) {
        const labels = history.map(h => new Date(h.recordedAt).toLocaleDateString());
        const data = history.map(h => h.price);

        this.chart = new Chart("priceHistoryChart", {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Price History',
                    data: data,
                    borderColor: 'rgb(75, 192, 192)',
                    tension: 0.1
                }]
            }
        });
    }
}
