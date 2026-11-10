import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { User } from '@shared/models/user.model';
import { environment } from '@environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
    localStorage.clear();
  });

  it('should be created and start without authenticated user', () => {
    expect(service).toBeTruthy();
    expect(service.isLoggedIn()).toBeFalse();
    expect(service.currentUserValue).toBeNull();
  });

  it('should authenticate user and store token and session on login', () => {
    const mockUser: User = {
      id: 'user-123',
      username: 'johndoe',
      email: 'john@example.com',
      role: 'USER',
      token: 'jwt-mock-token-xyz',
      tokenType: 'Bearer'
    };

    service.login({ username: 'johndoe', password: 'password123' }).subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpTestingController.expectOne(`${environment.apiUrl}/users/login`);
    expect(req.request.method).toEqual('POST');
    req.flush(mockUser);

    expect(service.isLoggedIn()).toBeTrue();
    expect(service.currentUserValue?.username).toBe('johndoe');
    expect(localStorage.getItem('auth_token')).toBe('jwt-mock-token-xyz');
    expect(JSON.parse(localStorage.getItem('currentUser') || '{}').username).toBe('johndoe');
  });

  it('should clear user state and localStorage on logout', () => {
    const mockUser: User = {
      id: 'user-123',
      username: 'johndoe',
      email: 'john@example.com',
      token: 'jwt-mock-token-xyz'
    };

    service.login({ username: 'johndoe', password: 'password123' }).subscribe();
    const req = httpTestingController.expectOne(`${environment.apiUrl}/users/login`);
    req.flush(mockUser);

    expect(service.isLoggedIn()).toBeTrue();

    service.logout();

    expect(service.isLoggedIn()).toBeFalse();
    expect(service.currentUserValue).toBeNull();
    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(localStorage.getItem('currentUser')).toBeNull();
  });

  it('should send registration request to backend', () => {
    const registrationData = {
      username: 'newuser',
      email: 'new@example.com',
      password: 'Password123'
    };

    const mockResponse: User = {
      id: 'user-456',
      username: 'newuser',
      email: 'new@example.com'
    };

    service.register(registrationData).subscribe(res => {
      expect(res.id).toBe('user-456');
    });

    const req = httpTestingController.expectOne(`${environment.apiUrl}/users/register`);
    expect(req.request.method).toEqual('POST');
    req.flush(mockResponse);
  });
});
