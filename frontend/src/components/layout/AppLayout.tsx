import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import './AppLayout.css'

export function AppLayout() {
  const { isAuthenticated, user, logout } = useAuth()

  return (
    <div className="app-shell">
      <a className="skip-link" href="#main-content">
        Skip to content
      </a>
      <header className="topbar">
        <div className="topbar__brand">
          <NavLink to="/" className="brand-link">
            Pokéfield
          </NavLink>
          <p className="brand-tag">Field notes for trainers</p>
        </div>
        <nav className="topbar__nav" aria-label="Primary">
          <NavLink to="/" end>
            Browse
          </NavLink>
          {isAuthenticated ? (
            <NavLink to="/local">Local roster</NavLink>
          ) : (
            <NavLink to="/login">Login</NavLink>
          )}
        </nav>
        {isAuthenticated && user ? (
          <div className="topbar__session">
            <span>
              {user.email} · {user.role}
            </span>
            <button type="button" className="ghost-button" onClick={logout}>
              Log out
            </button>
          </div>
        ) : null}
      </header>
      <main id="main-content" className="page">
        <Outlet />
      </main>
    </div>
  )
}
