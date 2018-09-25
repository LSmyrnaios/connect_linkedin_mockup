import { Component, OnInit } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { first } from 'rxjs/operators';

import { User } from '../../_models/index';
import {AlertService, AuthenticationService, UserService} from '../../_services/index';
import { MainNavBarComponent } from '../main-nav-bar/main-nav-bar.component';
import { FileUploaderService } from './../../_services/file-uploader/file-uploader.service';
import { PasswordConfirmValidatorDirective } from '../../_directives/validators/password-confirm-validator.directive';
import { TextValidatorDirective } from "../../_directives/validators/text_validator.directive";


@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.scss']
})
export class SignUpComponent implements OnInit {

  title = 'Sign Up to Connect';
  signUpForm: FormGroup;
  submitted = false;
  data: object;
  user: User;
  fileToUpload: File;

  constructor(
      private route: ActivatedRoute,
      private router: Router,
      private userService: UserService,
      private alertService: AlertService,
      private titleService: Title,
      private formBuilder: FormBuilder,
      private authenticationService: AuthenticationService,
      private fileUploader: FileUploaderService
  ) {
    this.titleService.setTitle(this.title);
  }

  minTextLength = 2;
  maxTextLength = 45;

  maxEmailLength = 65;

  minPasswordLength = 6;
  maxPasswordLength = 100;

  ngOnInit() {
    // Use FormBuilder to create a form group
    this.signUpForm = this.formBuilder.group({
      firstname: ['', [Validators.required, TextValidatorDirective.validateCharacters, Validators.minLength(this.minTextLength), Validators.maxLength(this.maxTextLength)]],
      lastname: ['', [Validators.required, TextValidatorDirective.validateCharacters, Validators.minLength(this.minTextLength), Validators.maxLength(this.maxTextLength)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(this.maxEmailLength)]],
      password: ['', [Validators.required, Validators.minLength(this.minPasswordLength), Validators.maxLength(this.maxPasswordLength)]],
      confirmPassword: ['', [PasswordConfirmValidatorDirective.validatePasswordConfirmation]]
    });
  }

  // Convenience getter for easy access to form fields
  get form() { return this.signUpForm.controls; }

  // Submits the form
  onSubmit() {
    this.submitted = true;

    // If form is invalid stop here
    if (this.signUpForm.invalid) {
      return;
    }

    // Create the user
    this.createUser(this.form.firstname.value, this.form.lastname.value, this.form.email.value, this.form.password.value, this.fileUploader.fileName);
  }

  createUser(firstname: string, lastname: string, email: string, password: string, picture: string) {
    // Trim email whitespace
    email = email.trim();

    const user: User = {firstname, lastname, email, password, picture} as User;

    // Send user to server
    this.userService.create(user)
      .pipe(first())
      .subscribe(
        data => {
          //this.data = data; // Unused..
          this.alertService.success('Registration successful.', true);
          this.fileUploader.postFile(email);  // Send photo to backend
          this.authenticationService.authenticateUserAndGoToHome(email, password);
        },
        error => {
          this.alertService.error(error.message);
        }
      );
  }

  onImageChange($event) {
    console.debug("Inside \"onImageChange()\"!");
    this.fileUploader.onImageChange($event)
  }

  onImageReset() {
    console.debug("Going to reset the \"fileToUpload\".");
    this.fileUploader.onFileReset();
  }

}
