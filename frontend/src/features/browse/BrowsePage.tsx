import { useEffect, useState } from 'react'
import { fetchPokemonPage } from '../../api/pokemonApi'
import { getErrorMessage } from '../../api/client'
import type { PokemonCardPage } from '../../types/pokemon'
import { Pagination } from './Pagination'
import { PokemonCard } from './PokemonCard'
import './BrowsePage.css'

const PAGE_SIZE = 12

export function BrowsePage() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState<PokemonCardPage | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    fetchPokemonPage(page, PAGE_SIZE)
      .then((result) => {
        if (!cancelled) {
          setData(result)
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setError(getErrorMessage(err, 'Unable to load Pokémon'))
          setData(null)
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [page])

  return (
    <section className="browse-page" aria-labelledby="browse-heading">
      <header className="browse-page__header">
        <h1 id="browse-heading">Pokémon field guide</h1>
        <p>Browse live PokéAPI data with sprites, categories, mass, and abilities.</p>
      </header>

      {loading ? (
        <div className="status-banner status-banner--loading" role="status" aria-live="polite">
          Loading Pokémon…
        </div>
      ) : null}

      {error ? (
        <div className="status-banner status-banner--error" role="alert">
          {error}
        </div>
      ) : null}

      {!loading && !error && data ? (
        <>
          <div className="grid-cards">
            {data.content.map((pokemon) => (
              <PokemonCard key={pokemon.name} pokemon={pokemon} />
            ))}
          </div>
          {data.content.length === 0 ? <p>No Pokémon found on this page.</p> : null}
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            onPageChange={setPage}
            disabled={loading}
          />
        </>
      ) : null}
    </section>
  )
}
