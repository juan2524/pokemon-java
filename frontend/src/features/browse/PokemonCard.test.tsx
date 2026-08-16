import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { PokemonCard } from './PokemonCard'

describe('PokemonCard', () => {
  it('renders sprite, category, weight, and abilities', () => {
    render(
      <MemoryRouter>
        <PokemonCard
          pokemon={{
            name: 'bulbasaur',
            spriteUrl: 'https://example.com/bulbasaur.png',
            category: 'Seed Pokémon',
            weightKg: 6.9,
            abilities: ['overgrow', 'chlorophyll'],
          }}
        />
      </MemoryRouter>,
    )

    expect(screen.getByRole('heading', { name: 'bulbasaur' })).toBeInTheDocument()
    expect(screen.getByAltText('bulbasaur sprite')).toHaveAttribute(
      'src',
      'https://example.com/bulbasaur.png',
    )
    expect(screen.getByText('Seed Pokémon')).toBeInTheDocument()
    expect(screen.getByText('6.9 kg')).toBeInTheDocument()
    expect(screen.getByText('overgrow')).toBeInTheDocument()
    expect(screen.getByText('chlorophyll')).toBeInTheDocument()
    expect(screen.getByRole('link')).toHaveAttribute('href', '/pokemon/bulbasaur')
  })
})
