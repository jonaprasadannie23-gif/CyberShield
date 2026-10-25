import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../AuthContext";

const links = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/incidents/new", label: "Report incident" },
  { to: "/incidents", label: "Incident history" },
  { to: "/recommendations", label: "Recommendations" },
  { to: "/tasks", label: "Safety tasks" },
  { to: "/password", label: "Password checker" },
  { to: "/reports", label: "Reports" },
];

export function Layout() {
  const { user, logout } = useAuth();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">CyberShield</div>
        {links.map((link) => (
          <NavLink key={link.to} to={link.to} className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}>
            {link.label}
          </NavLink>
        ))}
        <div className="sidebar-footer">
          <div>{user?.fullName}</div>
          <div className="muted">@{user?.username}</div>
          <button className="btn btn-secondary" style={{ marginTop: 12, width: "100%" }} onClick={() => logout()}>
            Sign out
          </button>
        </div>
      </aside>
      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
