import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../environments/environment';

const apiUrl = environment.apiUrl;
@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private http = inject(HttpClient);
  

  getTest() {
    return this.http.get(`${apiUrl}/test`, { responseType: 'text' });
  }
}
