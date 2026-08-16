import { Link, useParams } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { fetchPokemonDetail } from '../../api/pokemonApi'
import { getErrorMessage } from '../../api/client'
import type { PokemonDetail } from '../../types/pokemon'
import './DetailPage.css'

export function DetailPage() {
  const { idOrName = '' } = useParams()
  const [detail, setDetail] = useState<PokemonDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    fetchPokemonDetail(idOrName)
      .then((result) => {
        if (!cancelled) {
          setDetail(result)
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setError(getErrorMessage(err, 'Unable to load Pokémon detail'))
          setDetail(null)
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
  }, [idOrName])

  return (
    <section className="detail-page" aria-labelledby="detail-heading">
      <Link to="/" className="detail-page__back">
        ← Back to browse
      </Link>

      {loading ? (
        <div className="status-banner status-banner--loading" role="status">
          Loading detail…
        </div>
      ) : null}

      {error ? (
        <div className="status-banner status-banner--error" role="alert">
          {error}
        </div>
      ) : null}

      {detail ? (
        <div className="detail-layout panel">
          <div className="detail-layout__hero">
            {detail.imageUrl ? (
              <img src={detail.imageUrl} alt={`${detail.name} artwork`} />
            ) : (
              <div className="detail-layout__placeholder" aria-hidden="true">
                No image
              </div>
            )}
          </div>
          <div>
            <h1 id="detail-heading" className="detail-layout__title">
              {detail.name}
            </h1>
            <p className="detail-layout__types">{detail.types.join(' · ')}</p>
            <p>
              Height {detail.heightM.toFixed(1)} m · Weight {detail.weightKg.toFixed(1)} kg
            </p>
            <h2>Description</h2>
            <p>{detail.flavorTextEn ?? 'No English flavor text available.'}</p>

            <h2>Core stats</h2>
            <ul className="stat-list">
              {detail.stats.map((stat) => (
                <li key={stat.name}>
                  <div className="stat-list__label">
                    <span>{stat.name}</span>
                    <span>{stat.baseValue}</span>
                  </div>
                  <div
                    className="stat-list__track"
                    role="meter"
                    aria-label={`${stat.name} ${stat.baseValue}`}
                    aria-valuemin={0}
                    aria-valuemax={255}
                    aria-valuenow={stat.baseValue}
                  >
                    <span style={{ width: `${Math.min(100, (stat.baseValue / 255) * 100)}%` }} />
                  </div>
                </li>
              ))}
            </ul>

            <h2>Evolution lineage</h2>
            <ol className="evolution-list">
              {detail.evolutionLineage.map((node) => (
                <li key={node.name}>
                  <Link to={`/pokemon/${encodeURIComponent(node.name)}`}>{node.name}</Link>
                </li>
              ))}
            </ol>
          </div>
        </div>
      ) : null}
    </section>
  )
}
