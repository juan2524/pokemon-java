import { useEffect, useState, type FormEvent } from 'react'
import {
  deleteLocalPokemon,
  fetchLocalPokemon,
  syncPokemon,
  updateLocalPokemon,
} from '../../api/localPokemonApi'
import { getErrorMessage } from '../../api/client'
import { useAuth } from '../../auth/AuthContext'
import { ALLOWED_TAGS, type LocalPokemon } from '../../types/pokemon'
import './LocalPokemonPage.css'

export function LocalPokemonPage() {
  const { isAdmin } = useAuth()
  const [items, setItems] = useState<LocalPokemon[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)
  const [pokeApiId, setPokeApiId] = useState('25')
  const [syncing, setSyncing] = useState(false)
  const [selectedId, setSelectedId] = useState<string | null>(null)

  const selected = items.find((item) => item.id === selectedId) ?? null

  async function loadRoster() {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchLocalPokemon()
      setItems(data)
      if (data.length > 0) {
        setSelectedId((current) => current ?? data[0].id)
      } else {
        setSelectedId(null)
      }
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Unable to load local Pokémon'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadRoster()
  }, [])

  async function handleSync(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSyncing(true)
    setMessage(null)
    setError(null)
    try {
      const synced = await syncPokemon(Number(pokeApiId))
      setMessage(`Synced ${synced.name} (#${synced.pokeApiId})`)
      await loadRoster()
      setSelectedId(synced.id)
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Sync failed'))
    } finally {
      setSyncing(false)
    }
  }

  async function handleSave(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!selected) {
      return
    }
    const form = new FormData(event.currentTarget)
    const tags = ALLOWED_TAGS.filter((tag) => form.get(`tag-${tag}`) === 'on')
    setMessage(null)
    setError(null)
    try {
      const updated = await updateLocalPokemon(selected.id, {
        localizedName: String(form.get('localizedName') || '') || null,
        region: String(form.get('region') || '') || null,
        internalNotes: String(form.get('internalNotes') || '') || null,
        tags: [...tags],
        version: selected.version,
      })
      setMessage(`Updated ${updated.name}`)
      await loadRoster()
      setSelectedId(updated.id)
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Update failed'))
    }
  }

  async function handleDelete() {
    if (!selected || !isAdmin) {
      return
    }
    if (!window.confirm(`Delete local record for ${selected.name}?`)) {
      return
    }
    setMessage(null)
    setError(null)
    try {
      await deleteLocalPokemon(selected.id)
      setMessage(`Deleted ${selected.name}`)
      await loadRoster()
    } catch (err: unknown) {
      setError(getErrorMessage(err, 'Delete failed'))
    }
  }

  return (
    <section className="local-page" aria-labelledby="local-heading">
      <header>
        <h1 id="local-heading">Local roster</h1>
        <p>Synchronize PokéAPI entries and curate proprietary trainer notes.</p>
      </header>

      {loading ? (
        <div className="status-banner status-banner--loading" role="status">
          Loading local roster…
        </div>
      ) : null}
      {error ? (
        <div className="status-banner status-banner--error" role="alert">
          {error}
        </div>
      ) : null}
      {message ? (
        <div className="status-banner status-banner--loading" role="status">
          {message}
        </div>
      ) : null}

      {isAdmin ? (
        <form className="panel sync-form" onSubmit={handleSync}>
          <h2>Synchronize by PokéAPI ID</h2>
          <div className="field">
            <label htmlFor="pokeApiId">PokéAPI ID</label>
            <input
              id="pokeApiId"
              name="pokeApiId"
              type="number"
              min={1}
              required
              value={pokeApiId}
              onChange={(event) => setPokeApiId(event.target.value)}
            />
          </div>
          <button type="submit" className="primary-button" disabled={syncing}>
            {syncing ? 'Syncing…' : 'Sync Pokémon'}
          </button>
        </form>
      ) : (
        <div className="panel sync-form">
          <h2>Synchronize by PokéAPI ID</h2>
          <p className="local-actions__hint">Only ADMIN users can sync Pokémon from PokéAPI.</p>
        </div>
      )}

      <div className="local-layout">
        <div className="panel">
          <h2>Stored Pokémon</h2>
          {items.length === 0 && !loading ? <p>No local Pokémon yet. Sync one to begin.</p> : null}
          <ul className="local-list">
            {items.map((item) => (
              <li key={item.id}>
                <button
                  type="button"
                  className={item.id === selectedId ? 'local-list__item is-active' : 'local-list__item'}
                  onClick={() => setSelectedId(item.id)}
                >
                  <strong>{item.name}</strong>
                  <span>#{item.pokeApiId}</span>
                </button>
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Edit proprietary fields</h2>
          {!selected ? (
            <p>Select a Pokémon to edit.</p>
          ) : (
            <form key={selected.id} onSubmit={handleSave}>
              <p className="local-edit__title">
                {selected.name} · version {selected.version}
              </p>
              <div className="field">
                <label htmlFor="localizedName">Localized name</label>
                <input
                  id="localizedName"
                  name="localizedName"
                  defaultValue={selected.localizedName ?? ''}
                  maxLength={100}
                />
              </div>
              <div className="field">
                <label htmlFor="region">Region</label>
                <input id="region" name="region" defaultValue={selected.region ?? ''} maxLength={100} />
              </div>
              <div className="field">
                <label htmlFor="internalNotes">Internal notes</label>
                <textarea
                  id="internalNotes"
                  name="internalNotes"
                  rows={4}
                  defaultValue={selected.internalNotes ?? ''}
                  maxLength={2000}
                />
              </div>
              <fieldset className="tag-fieldset">
                <legend>Tags</legend>
                <div className="tag-grid">
                  {ALLOWED_TAGS.map((tag) => (
                    <label key={tag} className="tag-option">
                      <input
                        type="checkbox"
                        name={`tag-${tag}`}
                        defaultChecked={selected.tags.includes(tag)}
                      />
                      {tag}
                    </label>
                  ))}
                </div>
              </fieldset>
              <div className="local-actions">
                <button type="submit" className="primary-button">
                  Save changes
                </button>
                {isAdmin ? (
                  <button type="button" className="danger-button" onClick={() => void handleDelete()}>
                    Delete
                  </button>
                ) : (
                  <p className="local-actions__hint">Only ADMIN users can delete local records.</p>
                )}
              </div>
            </form>
          )}
        </div>
      </div>
    </section>
  )
}
