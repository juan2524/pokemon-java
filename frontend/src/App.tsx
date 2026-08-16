import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { AppLayout } from './components/layout/AppLayout'
import { BrowsePage } from './features/browse/BrowsePage'
import { DetailPage } from './features/detail/DetailPage'
import { LoginPage } from './features/auth/LoginPage'
import { LocalPokemonPage } from './features/local/LocalPokemonPage'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<AppLayout />}>
            <Route index element={<BrowsePage />} />
            <Route path="pokemon/:idOrName" element={<DetailPage />} />
            <Route path="login" element={<LoginPage />} />
            <Route
              path="local"
              element={
                <ProtectedRoute>
                  <LocalPokemonPage />
                </ProtectedRoute>
              }
            />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
