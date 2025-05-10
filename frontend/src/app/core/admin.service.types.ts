// src/app/core/admin.service.types.ts
export interface User {
    id: number;
    nome: string;
    username: string;
    email: string;
    institution: string;
    role: string;
    requestedRole: string | null;
    approved: boolean;
    approvalDate: string | null;
    adminComments: string | null;
    enabled: boolean;
    createdAt: string;
  }
  
  export interface UserApprovalRequest {
    approved: boolean;
    role?: string;
    adminComments?: string;
  }