import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AuthProvider } from '../../auth/AuthContext'
import { LoginPage } from './LoginPage'

const loginMock = vi.fn()

vi.mock('../../api/pokemonApi', () => ({
  login: (...args: unknown[]) => loginMock(...args),
  fetchPokemonPage: vi.fn(),
  fetchPokemonDetail: vi.fn(),
}))

function renderLogin() {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={['/login']}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/local" element={<div>Local roster ready</div>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>,
  )
}

describe('LoginPage', () => {
  beforeEach(() => {
    sessionStorage.clear()
    loginMock.mockReset()
  })

  it('signs in and redirects to the local roster', async () => {
    const user = userEvent.setup()
    loginMock.mockResolvedValue({
      accessToken: 'token-123',
      tokenType: 'Bearer',
      expiresIn: 3600,
      role: 'ADMIN',
      email: 'admin@example.com',
    })

    renderLogin()

    await user.clear(screen.getByLabelText('Email'))
    await user.type(screen.getByLabelText('Email'), 'admin@example.com')
    await user.clear(screen.getByLabelText('Password'))
    await user.type(screen.getByLabelText('Password'), 'secret')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    await waitFor(() => {
      expect(screen.getByText('Local roster ready')).toBeInTheDocument()
    })
    expect(loginMock).toHaveBeenCalledWith({
      email: 'admin@example.com',
      password: 'secret',
    })
    expect(sessionStorage.getItem('accessToken')).toBe('token-123')
    expect(sessionStorage.getItem('authRole')).toBe('ADMIN')
  })

  it('shows an error when login fails', async () => {
    const user = userEvent.setup()
    loginMock.mockRejectedValue(new Error('Invalid credentials'))

    renderLogin()

    await user.type(screen.getByLabelText('Email'), 'admin@example.com')
    await user.type(screen.getByLabelText('Password'), 'bad')
    await user.click(screen.getByRole('button', { name: 'Sign in' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Invalid credentials')
    expect(sessionStorage.getItem('accessToken')).toBeNull()
  })
})
