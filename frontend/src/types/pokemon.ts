export type Role = 'USER' | 'ADMIN'

export interface PokemonCard {
  name: string
  spriteUrl: string | null
  category: string | null
  weightKg: number
  abilities: string[]
}

export interface PokemonCardPage {
  content: PokemonCard[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface PokemonStat {
  name: string
  baseValue: number
}

export interface EvolutionNode {
  name: string
}

export interface PokemonDetail {
  name: string
  imageUrl: string | null
  heightM: number
  weightKg: number
  types: string[]
  stats: PokemonStat[]
  flavorTextEn: string | null
  evolutionLineage: EvolutionNode[]
}

export interface LocalPokemon {
  id: string
  pokeApiId: number
  name: string
  localizedName: string | null
  region: string | null
  internalNotes: string | null
  tags: string[]
  syncedAt: string
  version: number
}

export interface PatchLocalPokemonRequest {
  localizedName: string | null
  region: string | null
  internalNotes: string | null
  tags: string[]
  version: number
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  role: Role
  email: string
}

export interface ApiErrorBody {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  errors?: Array<{ field: string; message: string }>
}

export const ALLOWED_TAGS = [
  'starter',
  'legendary',
  'favorite',
  'team',
  'shiny',
  'competitive',
] as const
