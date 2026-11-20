import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface TrackableProduct {
    platformId: string;
    platform: string;
    isTracked: boolean;
}

export interface ProductTrackingResult extends TrackableProduct {
    id: number;
}

@Injectable({
    providedIn: 'root'
})
export class ProductTrackingService {
    private readonly trackingApi = `${environment.apiUrl}/scrape/track`;

    constructor(private http: HttpClient) {}

    setTracked(product: TrackableProduct): Observable<ProductTrackingResult> {
        const params = new HttpParams()
            .set('pid', product.platformId)
            .set('platform', product.platform)
            .set('isTracked', String(product.isTracked));

        return this.http.patch<ProductTrackingResult>(this.trackingApi, null, { params });
    }
}
