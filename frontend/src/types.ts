export type RiskLevel = "LOW" | "MEDIUM" | "HIGH";
export type IncidentStatus = "REPORTED" | "UNDER_REVIEW" | "RESOLVED";
export type IncidentType =
  | "PHISHING"
  | "SUSPICIOUS_LINK"
  | "OTP_SCAM"
  | "IMPERSONATION"
  | "SUSPICIOUS_EMAIL"
  | "OTHER";
export type IncidentSource =
  | "SMS"
  | "EMAIL"
  | "WEBSITE"
  | "SOCIAL_MEDIA"
  | "PHONE_CALL"
  | "OTHER";

export interface User {
  userId: string;
  username: string;
  email: string;
  fullName: string;
  registeredAt: string;
  lastLoginAt: string | null;
}

export interface Incident {
  incidentId: string;
  userId: string;
  title: string;
  type: IncidentType;
  description: string;
  source: IncidentSource;
  incidentDate: string;
  suspiciousUrl: string;
  riskLevel: RiskLevel;
  status: IncidentStatus;
  reportedAt: string;
  assessmentReasons: string[];
  recommendations: string[];
  typeSpecificDetails: string;
  claimedToBeLegitOrg?: boolean;
  containedAttachment?: boolean;
  linkWasClicked?: boolean;
  requestedCredentials?: boolean;
  otpWasShared?: boolean;
  financialInfoShared?: boolean;
}

export interface SecurityTask {
  taskId: string;
  userId: string;
  title: string;
  description: string;
  completed: boolean;
  createdAt: string;
  completedAt: string | null;
  relatedIncidentId?: string | null;
}

export interface DashboardData {
  totalIncidents: number;
  highRiskCount: number;
  unresolvedCount: number;
  pendingTasks: number;
  taskCompletionPercent: number;
  recentIncidents: Incident[];
  pendingTaskList: SecurityTask[];
  byRisk: Record<RiskLevel, number>;
  byType: Record<IncidentType, number>;
  byStatus: Record<IncidentStatus, number>;
}

export interface ReportData {
  totalIncidents: number;
  resolvedCount: number;
  unresolvedCount: number;
  byRisk: Record<RiskLevel, number>;
  byType: Record<IncidentType, number>;
  byStatus: Record<IncidentStatus, number>;
  totalTasks: number;
  completedTasks: number;
  pendingTasks: number;
  taskCompletionPercent: number;
}

export interface RecommendationsData {
  recommendations: string[];
  disclaimer: string[];
  totalIncidents?: number;
  totalTasks?: number;
  completedTasks?: number;
}

export const TYPE_LABELS: Record<IncidentType, string> = {
  PHISHING: "Phishing",
  SUSPICIOUS_LINK: "Suspicious Link",
  OTP_SCAM: "OTP Scam",
  IMPERSONATION: "Impersonation",
  SUSPICIOUS_EMAIL: "Suspicious Email",
  OTHER: "Other",
};

export const SOURCE_LABELS: Record<IncidentSource, string> = {
  SMS: "SMS",
  EMAIL: "Email",
  WEBSITE: "Website",
  SOCIAL_MEDIA: "Social Media",
  PHONE_CALL: "Phone Call",
  OTHER: "Other",
};

export const STATUS_LABELS: Record<IncidentStatus, string> = {
  REPORTED: "Reported",
  UNDER_REVIEW: "Under Review",
  RESOLVED: "Resolved",
};
