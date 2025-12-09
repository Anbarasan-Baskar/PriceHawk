import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

const AUTH_API = environment.apiUrl + '/auth/';
const PRODUCT_API = environment.apiUrl + '/products';

@Injectable({
    providedIn: 'root'
})
export class ApiService {
    updateTrackStatus(product: any) {
        return this.http.patch(`http://localhost:8080/api/products/${product.id}/track`, {
            isTracked: product.isTracked
        });
    }

    constructor(private http: HttpClient) { }

    login(credentials: any): Observable<any> {
        return this.http.post(AUTH_API + 'signin', credentials);
    }

    register(user: any): Observable<any> {
        return this.http.post(AUTH_API + 'signup', user);
    }

    getProducts(): Observable<any> {
        return this.http.get(PRODUCT_API);
    }

    getProduct(id: any): Observable<any> {
        return this.http.get(PRODUCT_API + '/' + id);
    }

    getBestDeal(id: any): Observable<any> {
        return this.http.get(PRODUCT_API + '/' + id + '/best-deal');
    }

    addToWatchlist(productId: number, targetPrice: number, token: string): Observable<any> {
        const headers = new HttpHeaders().set('Authorization', 'Bearer ' + token);
        return this.http.post(environment.apiUrl + '/watchlist', { productId, targetPrice }, { headers });
    }

    getPriceHistory(productId: number): Observable<any> {
        return this.http.get(environment.apiUrl + '/history/' + productId);
    }
}
