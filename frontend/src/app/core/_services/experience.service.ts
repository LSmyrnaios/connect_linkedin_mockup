import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Experience } from '../_models/index';
import { ConnectionConfigService } from './connection-config.service';

@Injectable()
export class ExperienceService {

  constructor(
    private httpClient: HttpClient,
    private connConfig: ConnectionConfigService
  ) { }

  // getAll() {
  //   return this.httpClient.get<User[]>(this.connConfig.serverUrl + this.connConfig.signupEndpoint);
  // }

  // getById(id: number): Observable<User> {
  //   return this.httpClient.get<User>(this.connConfig.serverUrl + this.connConfig.signupEndpoint + '/' + id);
  // }

  create(experience: Experience, userId: number) {
    return this.httpClient.post(this.connConfig.serverUrl + this.connConfig.userEndpoint + '/' +
        userId + '/' + this.connConfig.experienceEndpoint, experience);
  }

  // update(user: User) {
  //   return this.httpClient.put('/api/users/' + user.id, user);
  // }

  // delete(id: number) {
  //   return this.httpClient.delete('/api/users/' + id);
  // }
}