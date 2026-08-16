import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { login as loginRequest } from '../api/pokemonApi'
import type { LoginRequest, Role } from '../types/pokemon'

interface AuthUser {
  email: string
  role: Role
}

interface AuthContextValue {
  user: AuthUser | null
  token: string | null
  isAuthenticated: boolean
  isAdmin: boolean
  login: (credentials: LoginRequest) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function readStoredUser(): AuthUser | null {
  const email = sessionStorage.getItem('authEmail')
  const role = sessionStorage.getItem('authRole') as Role | null
  if (!email || !role) {
    return null
  }
  return { email, role }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => sessionStorage.getItem('accessToken'))
  const [user, setUser] = useState<AuthUser | null>(() => readStoredUser())

  const login = useCallback(async (credentials: LoginRequest) => {
    const response = await loginRequest(credentials)
    sessionStorage.setItem('accessToken', response.accessToken)
    sessionStorage.setItem('authRole', response.role)
    sessionStorage.setItem('authEmail', response.email)
    setToken(response.accessToken)
    setUser({ email: response.email, role: response.role })
  }, [])

  const logout = useCallback(() => {
    sessionStorage.removeItem('accessToken')
    sessionStorage.removeItem('authRole')
    sessionStorage.removeItem('authEmail')
    setToken(null)
    setUser(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      token,
      isAuthenticated: Boolean(token && user),
      isAdmin: user?.role === 'ADMIN',
      login,
      logout,
    }),
    [user, token, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}
