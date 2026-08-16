import { apiClient } from './client'
import type { LoginRequest, LoginResponse, PokemonCardPage, PokemonDetail } from '../types/pokemon'

export async function fetchPokemonPage(page: number, size: number): Promise<PokemonCardPage> {
  const { data } = await apiClient.get<PokemonCardPage>('/api/v1/pokemon', {
    params: { page, size },
  })
  return data
}

export async function fetchPokemonDetail(idOrName: string): Promise<PokemonDetail> {
  const { data } = await apiClient.get<PokemonDetail>(
    `/api/v1/pokemon/${encodeURIComponent(idOrName)}`,
  )
  return data
}

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const { data } = await apiClient.post<LoginResponse>('/api/v1/auth/login', request)
  return data
}
