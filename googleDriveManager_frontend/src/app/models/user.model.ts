export interface User {
  id: number;
  username: string;
  role: string;
  password?: string;
}

export interface CreateUserRequest {
  username: string;
  password?: string;
  role?: string;
}

export interface UpdateUserRequest {
  username?: string;
  password?: string;
  role?: string;
}
