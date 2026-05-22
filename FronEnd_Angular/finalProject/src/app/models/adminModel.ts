export interface SignupRequest {
  username: string;
  firstName: string;
  lastName: string;
  academicEmail: string;
  password?: string; // Optional because we might auto-generate it later or require it
  role: string; // Based on our previous @ManyToOne refactor
  degreeName?: string; // Optional, only needed if the user is a student
}

export interface MessageResponse {
  message: string;
}
