import { apiClient } from './client'
import type { LocalPokemon, PatchLocalPokemonRequest } from '../types/pokemon'

export async function fetchLocalPokemon(): Promise<LocalPokemon[]> {
  const { data } = await apiClient.get<LocalPokemon[]>('/api/v1/local/pokemon')
  return data
}

export async function syncPokemon(pokeApiId: number): Promise<LocalPokemon> {
  const { data } = await apiClient.post<LocalPokemon>(`/api/v1/pokemon/${pokeApiId}/sync`)
  return data
}

export async function updateLocalPokemon(
  id: string,
  body: PatchLocalPokemonRequest,
): Promise<LocalPokemon> {
  const { data } = await apiClient.patch<LocalPokemon>(`/api/v1/local/pokemon/${id}`, body)
  return data
}

export async function deleteLocalPokemon(id: string): Promise<void> {
  await apiClient.delete(`/api/v1/local/pokemon/${id}`)
}
