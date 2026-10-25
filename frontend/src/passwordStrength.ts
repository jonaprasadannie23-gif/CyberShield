export type StrengthLevel = "VERY_WEAK" | "WEAK" | "FAIR" | "STRONG" | "VERY_STRONG";

export interface StrengthResult {
  level: StrengthLevel;
  label: string;
  score: number;
  suggestions: string[];
  disclaimer: string;
}

const LABELS: Record<StrengthLevel, string> = {
  VERY_WEAK: "Very Weak",
  WEAK: "Weak",
  FAIR: "Fair",
  STRONG: "Strong",
  VERY_STRONG: "Very Strong",
};

function isRepeatingChars(s: string): boolean {
  if (s.length < 4) return false;
  for (let i = 0; i <= s.length - 4; i++) {
    const c = s[i];
    if (c === s[i + 1] && c === s[i + 2] && c === s[i + 3]) return true;
  }
  return false;
}

/** Evaluates locally. The password never leaves the browser. */
export function evaluatePassword(password: string): StrengthResult {
  const suggestions: string[] = [];
  let score = 0;
  const disclaimer =
    "This is an educational strength estimate only. It does not determine whether your password has been leaked or compromised in a data breach.";

  if (!password) {
    return { level: "VERY_WEAK", label: LABELS.VERY_WEAK, score: 0, suggestions: ["Enter a password to evaluate."], disclaimer };
  }

  const len = password.length;
  if (len < 8) suggestions.push("Use at least 8 characters.");
  else if (len < 12) {
    score += 15;
    suggestions.push("Consider using 12 or more characters for better strength.");
  } else if (len < 16) score += 25;
  else score += 35;

  const hasUpper = /[A-Z]/.test(password);
  const hasLower = /[a-z]/.test(password);
  const hasDigit = /\d/.test(password);
  const hasSpecial = /[!@#$%^&*()_+\-=[\]{}|;':",./<>?]/.test(password);

  if (hasUpper) score += 15; else suggestions.push("Add uppercase letters (A–Z).");
  if (hasLower) score += 15; else suggestions.push("Add lowercase letters (a–z).");
  if (hasDigit) score += 15; else suggestions.push("Include numbers (0–9).");
  if (hasSpecial) score += 20; else suggestions.push("Include special characters (e.g. !@#$%^&*).");

  const lc = password.toLowerCase();
  if (lc.includes("password") || lc.includes("123456") || lc.includes("qwerty")) {
    score -= 20;
    suggestions.push("Avoid common patterns like 'password', '123456', or 'qwerty'.");
  }
  if (isRepeatingChars(password)) {
    score -= 10;
    suggestions.push("Avoid repeating the same character many times (e.g. 'aaaa').");
  }

  score = Math.max(0, Math.min(100, score));
  let level: StrengthLevel;
  if (score < 20) level = "VERY_WEAK";
  else if (score < 40) level = "WEAK";
  else if (score < 60) level = "FAIR";
  else if (score < 80) level = "STRONG";
  else level = "VERY_STRONG";

  if (suggestions.length === 0) {
    suggestions.push("Great password! Remember to use a unique password for every account.");
  }

  return { level, label: LABELS[level], score, suggestions, disclaimer };
}
