import { useMemo, useState } from "react";
import { evaluatePassword } from "../passwordStrength";

const COLORS: Record<string, string> = {
  VERY_WEAK: "#f85149",
  WEAK: "#e67e22",
  FAIR: "#d29922",
  STRONG: "#3fb950",
  VERY_STRONG: "#20c997",
};

export function PasswordPage() {
  const [password, setPassword] = useState("");
  const result = useMemo(() => evaluatePassword(password), [password]);

  return (
    <>
      <h1 className="page-title">Password strength checker</h1>
      <p className="page-sub">Evaluated in your browser only. The value is never sent to the server or stored.</p>
      <div className="card">
        <label htmlFor="pw">Candidate password</label>
        <input id="pw" type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="off" />
        <div className="strength-bar">
          <span style={{ width: `${result.score}%`, background: COLORS[result.level] }} />
        </div>
        <p><strong>{result.label}</strong> · score {result.score}/100</p>
        <ul>{result.suggestions.map((item) => <li key={item}>{item}</li>)}</ul>
        <button className="btn btn-secondary" onClick={() => setPassword("")}>Clear</button>
        <p className="muted">{result.disclaimer}</p>
      </div>
    </>
  );
}
