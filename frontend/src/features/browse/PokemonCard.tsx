import { Link } from 'react-router-dom'
import type { PokemonCard as PokemonCardData } from '../../types/pokemon'
import './PokemonCard.css'

interface PokemonCardProps {
  pokemon: PokemonCardData
}

export function PokemonCard({ pokemon }: PokemonCardProps) {
  return (
    <article className="poke-card">
      <Link to={`/pokemon/${encodeURIComponent(pokemon.name)}`} className="poke-card__link">
        <div className="poke-card__media">
          {pokemon.spriteUrl ? (
            <img src={pokemon.spriteUrl} alt={`${pokemon.name} sprite`} loading="lazy" />
          ) : (
            <div className="poke-card__placeholder" aria-hidden="true">
              ?
            </div>
          )}
        </div>
        <h2 className="poke-card__name">{pokemon.name}</h2>
        <p className="poke-card__meta">{pokemon.category ?? 'Unknown category'}</p>
        <p className="poke-card__meta">{pokemon.weightKg.toFixed(1)} kg</p>
        <ul className="poke-card__abilities" aria-label={`${pokemon.name} abilities`}>
          {pokemon.abilities.map((ability) => (
            <li key={ability}>{ability}</li>
          ))}
        </ul>
      </Link>
    </article>
  )
}
