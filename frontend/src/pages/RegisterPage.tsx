import { FormEvent, useId, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { ApiError } from "../api";

// ---------------------------------------------------------------------------
// Inline SVG icons — identical set to LoginPage so the two pages look uniform
// ---------------------------------------------------------------------------

function ShieldCheckIcon() {
  return (
    <svg aria-hidden="true" width="28" height="28" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2"
      strokeLinecap="round" strokeLinejoin="round">
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
      <polyline points="9 12 11 14 15 10" />
    </svg>
  );
}

function UserIcon() {
  return (
    <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2"
      strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="8" r="4" />
      <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
    </svg>
  );
}

function MailIcon() {
  return (
    <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2"
      strokeLinecap="round" strokeLinejoin="round">
      <rect x="2" y="4" width="20" height="16" rx="2" />
      <polyline points="2,4 12,13 22,4" />
    </svg>
  );
}

function PersonIcon() {
  return (
    <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2"
      strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2"
      strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
      <path d="M7 11V7a5 5 0 0 1 10 0v4" />
    </svg>
  );
}

function EyeIcon() {
  return (
    <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2"
      strokeLinecap="round" strokeLinejoin="round">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

function EyeOffIcon() {
  return (
    <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2"
      strokeLinecap="round" strokeLinejoin="round">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
      <line x1="1" y1="1" x2="23" y2="23" />
    </svg>
  );
}

function AlertIcon() {
  return (
    <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2"
      strokeLinecap="round" strokeLinejoin="round"
      style={{ flexShrink: 0, marginTop: 1 }}>
      <circle cx="12" cy="12" r="10" />
      <line x1="12" y1="8" x2="12" y2="12" />
      <line x1="12" y1="16" x2="12.01" y2="16" />
    </svg>
  );
}

function SpinnerIcon() {
  return (
    <svg aria-hidden="true" width="16" height="16" viewBox="0 0 24 24"
      fill="none" stroke="currentColor" strokeWidth="2.5"
      strokeLinecap="round" className="login-spinner">
      <path d="M12 2a10 10 0 1 0 10 10" />
    </svg>
  );
}

// ---------------------------------------------------------------------------
// Field-level validation helpers
// ---------------------------------------------------------------------------

function validateUsername(v: string): string {
  if (!v.trim()) return "Username is required.";
  if (!/^[A-Za-z0-9_]{3,20}$/.test(v.trim()))
    return "3–20 characters: letters, digits, or underscores only.";
  return "";
}

function validateEmail(v: string): string {
  if (!v.trim()) return "Email address is required.";
  if (!/^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(v.trim()))
    return "Please enter a valid email address.";
  return "";
}

function validatePassword(v: string): string {
  if (!v) return "Password is required.";
  if (v.length < 8) return "Password must be at least 8 characters.";
  return "";
}

// ---------------------------------------------------------------------------
// Registration page
// ---------------------------------------------------------------------------

export function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [fullName, setFullName] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  // Track which fields the user has left (to show inline errors on blur, not on load)
  const [touched, setTouched] = useState({
    username: false,
    email: false,
    password: false,
  });

  const [apiError, setApiError] = useState("");
  const [busy, setBusy] = useState(false);

  const usernameId = useId();
  const emailId    = useId();
  const fullNameId = useId();
  const passwordId = useId();
  const errorId    = useId();

  // Inline errors (only shown after the field has been touched or submit was attempted)
  const errors = {
    username: touched.username ? validateUsername(username) : "",
    email:    touched.email    ? validateEmail(email)       : "",
    password: touched.password ? validatePassword(password) : "",
  };

  function touch(field: keyof typeof touched) {
    setTouched((prev) => ({ ...prev, [field]: true }));
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault();

    // Touch all required fields so inline errors become visible
    setTouched({ username: true, email: true, password: true });

    // Client-side gate — do not send if any required field is invalid
    if (validateUsername(username) || validateEmail(email) || validatePassword(password)) {
      return;
    }

    setApiError("");
    setBusy(true);
    try {
      // AuthContext.register() posts to /api/auth/register, receives the user,
      // sets the auth state (auto-signin), then the Guard in App.tsx redirects.
      // We navigate to /dashboard to match the established app flow.
      await register({ username: username.trim(), email: email.trim(), fullName, password });
      navigate("/dashboard");
    } catch (err) {
      if (err instanceof ApiError) {
        setApiError(err.message);
      } else {
        setApiError("Registration failed. Please check your connection and try again.");
      }
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login-page">
      {/* ── Brand header ──────────────────────────────── */}
      <header className="login-brand">
        <span className="login-brand-icon">
          <ShieldCheckIcon />
        </span>
        <span className="login-brand-name">CyberShield</span>
      </header>

      {/* ── Card ──────────────────────────────────────── */}
      <main className="login-card" role="main">
        <div className="login-card-header">
          <h1 className="login-heading">Create account</h1>
          <p className="login-subheading">
            Your incidents and safety tasks are isolated to your account.
          </p>
        </div>

        {/* API-level error banner */}
        {apiError && (
          <div
            className="login-error-banner"
            role="alert"
            aria-live="assertive"
            id={errorId}
          >
            <AlertIcon />
            <span>{apiError}</span>
          </div>
        )}

        <form
          onSubmit={onSubmit}
          noValidate
          aria-describedby={apiError ? errorId : undefined}
        >
          {/* ── Username ── */}
          <div className="login-field">
            <label htmlFor={usernameId} className="login-label">
              Username <span aria-hidden="true" style={{ color: "var(--risk-high)" }}>*</span>
            </label>
            <div className={`login-input-wrap${errors.username ? " invalid" : ""}`}>
              <span className="login-input-icon"><UserIcon /></span>
              <input
                id={usernameId}
                name="username"
                className="login-input"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                onBlur={() => touch("username")}
                autoComplete="username"
                autoCapitalize="none"
                spellCheck={false}
                aria-invalid={!!errors.username}
                aria-describedby={errors.username ? `${usernameId}-err` : undefined}
                aria-required="true"
                disabled={busy}
              />
            </div>
            {errors.username && (
              <p className="login-field-error" id={`${usernameId}-err`} role="alert">
                {errors.username}
              </p>
            )}
          </div>

          {/* ── Email ── */}
          <div className="login-field">
            <label htmlFor={emailId} className="login-label">
              Email <span aria-hidden="true" style={{ color: "var(--risk-high)" }}>*</span>
            </label>
            <div className={`login-input-wrap${errors.email ? " invalid" : ""}`}>
              <span className="login-input-icon"><MailIcon /></span>
              <input
                id={emailId}
                name="email"
                className="login-input"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                onBlur={() => touch("email")}
                autoComplete="email"
                spellCheck={false}
                aria-invalid={!!errors.email}
                aria-describedby={errors.email ? `${emailId}-err` : undefined}
                aria-required="true"
                disabled={busy}
              />
            </div>
            {errors.email && (
              <p className="login-field-error" id={`${emailId}-err`} role="alert">
                {errors.email}
              </p>
            )}
          </div>

          {/* ── Full name (optional) ── */}
          <div className="login-field">
            <label htmlFor={fullNameId} className="login-label">
              Full name{" "}
              <span style={{ color: "var(--muted)", fontWeight: 400, fontSize: "0.8rem" }}>
                (optional)
              </span>
            </label>
            <div className="login-input-wrap">
              <span className="login-input-icon"><PersonIcon /></span>
              <input
                id={fullNameId}
                name="fullName"
                className="login-input"
                type="text"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                autoComplete="name"
                disabled={busy}
              />
            </div>
          </div>

          {/* ── Password ── */}
          <div className="login-field">
            <label htmlFor={passwordId} className="login-label">
              Password <span aria-hidden="true" style={{ color: "var(--risk-high)" }}>*</span>
            </label>
            <div className={`login-input-wrap${errors.password ? " invalid" : ""}`}>
              <span className="login-input-icon"><LockIcon /></span>
              <input
                id={passwordId}
                name="password"
                className="login-input"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onBlur={() => touch("password")}
                autoComplete="new-password"
                aria-invalid={!!errors.password}
                aria-describedby={errors.password ? `${passwordId}-err` : undefined}
                aria-required="true"
                disabled={busy}
              />
              <button
                type="button"
                className="login-eye-btn"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? "Hide password" : "Show password"}
                disabled={busy}
              >
                {showPassword ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
            {errors.password && (
              <p className="login-field-error" id={`${passwordId}-err`} role="alert">
                {errors.password}
              </p>
            )}
          </div>

          {/* ── Submit ── */}
          <button
            className="login-submit"
            type="submit"
            disabled={busy}
            aria-busy={busy}
          >
            {busy ? (
              <>
                <SpinnerIcon />
                Creating account…
              </>
            ) : (
              "Create account"
            )}
          </button>
        </form>

        <p className="login-register-link">
          Already have an account?{" "}
          <Link to="/login">Sign in</Link>
        </p>
      </main>

      <p className="login-tagline">Your digital safety, under control.</p>
    </div>
  );
}
