import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { User } from '../_models/index';
import { ConnectionConfigService } from './connection-config.service';

@Injectable()
export class UserService {

  constructor(
    private httpClient: HttpClient,
    private connConfig: ConnectionConfigService
  ) { }

  getAll() {
    return this.httpClient.get<User[]>(this.connConfig.usersEndpoint);
  }

  getById(id: number): Observable<User> {
    return this.httpClient.get<User>(this.connConfig.usersEndpoint + '/' + id);
  }

  getByEmail(email: string): Observable<User> {
    return this.httpClient.get<User>(this.connConfig.usersEndpoint + '/getUserByEmail',
      {params: { userEmail: email }}
    );
  }

  create(user: User) {
    return this.httpClient.post(this.connConfig.usersEndpoint, user);
  }

  update(user: User) {
    return this.httpClient.put(this.connConfig.usersEndpoint + '/' + user.id, user);
  }

  delete(id: number) {
    return this.httpClient.delete(this.connConfig.usersEndpoint + '/' + id);
  }

}
