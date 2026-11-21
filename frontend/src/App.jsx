import { useEffect, useState } from 'react'
import axios from 'axios'

const API = '/api/vevok'

// Ha másik táblát használsz: csak ezt a tömböt (és a Java entitást) kell átírni.
const FIELDS = [
  { name: 'nev', label: 'Név', required: true },
  { name: 'email', label: 'E-mail', type: 'email' },
  { name: 'varos', label: 'Város' },
  { name: 'telefon', label: 'Telefon' }
]

const emptyForm = () => Object.fromEntries(FIELDS.map(f => [f.name, '']))

export default function App() {
  const [items, setItems] = useState([])
  const [form, setForm] = useState(emptyForm())
  const [editingId, setEditingId] = useState(null)
  const [error, setError] = useState('')

  const load = async () => {
    try {
      const res = await axios.get(API)
      setItems(res.data)
      setError('')
    } catch (e) {
      setError('Nem sikerült betölteni az adatokat.')
    }
  }

  useEffect(() => { load() }, [])

  const onChange = e => setForm({ ...form, [e.target.name]: e.target.value })

  const onSubmit = async e => {
    e.preventDefault()
    try {
      if (editingId === null) {
        await axios.post(API, form)          // Create
      } else {
        await axios.put(`${API}/${editingId}`, form)   // Update
      }
      setForm(emptyForm())
      setEditingId(null)
      await load()                            // Read
    } catch (err) {
      setError(err.response?.data?.errors?.[0]?.defaultMessage || 'Mentési hiba.')
    }
  }

  const onEdit = item => {
    setEditingId(item.id)
    setForm(Object.fromEntries(FIELDS.map(f => [f.name, item[f.name] ?? ''])))
  }

  const onDelete = async id => {
    if (!window.confirm('Biztosan törlöd?')) return
    try {
      await axios.delete(`${API}/${id}`)      // Delete
      await load()
    } catch (err) {
      setError('Törlési hiba.')
    }
  }

  const onCancel = () => { setEditingId(null); setForm(emptyForm()) }

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 className="mb-0">React CRUD – Vevők (H2 adatbázis)</h2>
        <a className="btn btn-outline-primary" href="/">Vissza a főoldalra</a>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      <form onSubmit={onSubmit} className="row g-3 align-items-end mb-4">
        {FIELDS.map(f => (
          <div className="col-md-3" key={f.name}>
            <label className="form-label" htmlFor={f.name}>{f.label}</label>
            <input
              className="form-control"
              id={f.name}
              name={f.name}
              type={f.type || 'text'}
              value={form[f.name]}
              onChange={onChange}
              required={f.required}
            />
          </div>
        ))}
        <div className="col-12">
          <button className="btn btn-primary me-2" type="submit">
            {editingId === null ? 'Hozzáadás' : 'Mentés'}
          </button>
          {editingId !== null && (
            <button className="btn btn-secondary" type="button" onClick={onCancel}>Mégse</button>
          )}
        </div>
      </form>

      <div className="table-responsive">
        <table className="table table-striped align-middle">
          <thead>
            <tr>
              <th>ID</th>
              {FIELDS.map(f => <th key={f.name}>{f.label}</th>)}
              <th></th>
            </tr>
          </thead>
          <tbody>
            {items.map(item => (
              <tr key={item.id}>
                <td>{item.id}</td>
                {FIELDS.map(f => <td key={f.name}>{item[f.name]}</td>)}
                <td className="text-end">
                  <button className="btn btn-sm btn-outline-primary me-2" onClick={() => onEdit(item)}>Szerkesztés</button>
                  <button className="btn btn-sm btn-outline-danger" onClick={() => onDelete(item.id)}>Törlés</button>
                </td>
              </tr>
            ))}
            {items.length === 0 && (
              <tr><td colSpan={FIELDS.length + 2} className="text-center text-body-secondary">Nincs adat.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
