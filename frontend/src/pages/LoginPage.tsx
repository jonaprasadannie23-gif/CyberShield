import { FormEvent, useId, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../AuthContext";
import { ApiError } from "../api";

// ---------------------------------------------------------------------------
// Inline SVG icons – no external icon library required
// ---------------------------------------------------------------------------

function ShieldCheckIcon() {
  return (
    <svg
      aria-hidden="true"
      width="28"
      height="28"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
      <polyline points="9 12 11 14 15 10" />
    </svg>
  );
}

function UserIcon() {
  return (
    <svg
      aria-hidden="true"
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="12" cy="8" r="4" />
      <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
    </svg>
  );
}

function LockIcon() {
  return (
    <svg
      aria-hidden="true"
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
      <path d="M7 11V7a5 5 0 0 1 10 0v4" />
    </svg>
  );
}

function EyeIcon() {
  return (
    <svg
      aria-hidden="true"
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}

function EyeOffIcon() {
  return (
    <svg
      aria-hidden="true"
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
      <line x1="1" y1="1" x2="23" y2="23" />
    </svg>
  );
}

function AlertIcon() {
  return (
    <svg
      aria-hidden="true"
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      style={{ flexShrink: 0, marginTop: 1 }}
    >
      <circle cx="12" cy="12" r="10" />
      <line x1="12" y1="8" x2="12" y2="12" />
      <line x1="12" y1="16" x2="12.01" y2="16" />
    </svg>
  );
}

function SpinnerIcon() {
  return (
    <svg
      aria-hidden="true"
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.5"
      strokeLinecap="round"
      className="login-spinner"
    >
      <path d="M12 2a10 10 0 1 0 10 10" />
    </svg>
  );
}

// ---------------------------------------------------------------------------
// Login page
// ---------------------------------------------------------------------------

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  // Field-level validation messages (shown on blur)
  const [usernameBlurred, setUsernameBlurred] = useState(false);
  const [passwordBlurred, setPasswordBlurred] = useState(false);

  const usernameId = useId();
  const passwordId = useId();
  const errorId = useId();

  const usernameInvalid = usernameBlurred && username.trim().length === 0;
  const passwordInvalid = passwordBlurred && password.length === 0;

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    // Touch both fields so inline errors show
    setUsernameBlurred(true);
    setPasswordBlurred(true);
    if (!username.trim() || !password) return;

    setError("");
    setBusy(true);
    try {
      await login(username.trim(), password);
      navigate("/dashboard");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Unable to sign in. Please try again.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login-page">
      {/* ── Brand header ─────────────────────────────── */}
      <header className="login-brand">
        <span className="login-brand-icon">
          <ShieldCheckIcon />
        </span>
        <span className="login-brand-name">CyberShield</span>
      </header>

      {/* ── Card ─────────────────────────────────────── */}
      <main className="login-card" role="main">
        <div className="login-card-header">
          <h1 className="login-heading">Welcome back!</h1>
          <p className="login-subheading">
            Manage your personal cyber incidents, assess risks, and track safety tasks — all in one place.
          </p>
        </div>

        {/* Auth error banner */}
        {error && (
          <div
            className="login-error-banner"
            role="alert"
            aria-live="assertive"
            id={errorId}
          >
            <AlertIcon />
            <span>{error}</span>
          </div>
        )}

        <form
          onSubmit={onSubmit}
          noValidate
          aria-describedby={error ? errorId : undefined}
        >
          {/* ── Username field ── */}
          <div className="login-field">
            <label htmlFor={usernameId} className="login-label">
              Username
            </label>
            <div className={`login-input-wrap${usernameInvalid ? " invalid" : ""}`}>
              <span className="login-input-icon">
                <UserIcon />
              </span>
              <input
                id={usernameId}
                name="username"
                className="login-input"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                onBlur={() => setUsernameBlurred(true)}
                autoComplete="username"
                autoCapitalize="none"
                spellCheck={false}
                aria-invalid={usernameInvalid}
                aria-describedby={usernameInvalid ? `${usernameId}-err` : undefined}
                disabled={busy}
              />
            </div>
            {usernameInvalid && (
              <p className="login-field-error" id={`${usernameId}-err`} role="alert">
                Username is required.
              </p>
            )}
          </div>

          {/* ── Password field ── */}
          <div className="login-field">
            <label htmlFor={passwordId} className="login-label">
              Password
            </label>
            <div className={`login-input-wrap${passwordInvalid ? " invalid" : ""}`}>
              <span className="login-input-icon">
                <LockIcon />
              </span>
              <input
                id={passwordId}
                name="password"
                className="login-input"
                type={showPassword ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onBlur={() => setPasswordBlurred(true)}
                autoComplete="current-password"
                aria-invalid={passwordInvalid}
                aria-describedby={passwordInvalid ? `${passwordId}-err` : undefined}
                disabled={busy}
              />
              <button
                type="button"
                className="login-eye-btn"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? "Hide password" : "Show password"}
                tabIndex={0}
                disabled={busy}
              >
                {showPassword ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
            {passwordInvalid && (
              <p className="login-field-error" id={`${passwordId}-err`} role="alert">
                Password is required.
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
                Signing in…
              </>
            ) : (
              "Sign in"
            )}
          </button>
        </form>

        <p className="login-register-link">
          Don't have an account?{" "}
          <Link to="/register">Create one</Link>
        </p>
      </main>

      <p className="login-tagline">Your digital safety, under control.</p>
    </div>
  );
}
