interface PaginationProps {
  page: number
  totalPages: number
  onPageChange: (page: number) => void
  disabled?: boolean
}

export function Pagination({ page, totalPages, onPageChange, disabled }: PaginationProps) {
  const canPrev = page > 0
  const canNext = page + 1 < totalPages

  return (
    <nav className="pagination" aria-label="Pokémon pagination">
      <button
        type="button"
        className="ghost-button"
        disabled={disabled || !canPrev}
        onClick={() => onPageChange(page - 1)}
      >
        Previous
      </button>
      <p className="pagination__status">
        Page <span aria-current="page">{page + 1}</span> of {Math.max(totalPages, 1)}
      </p>
      <button
        type="button"
        className="ghost-button"
        disabled={disabled || !canNext}
        onClick={() => onPageChange(page + 1)}
      >
        Next
      </button>
    </nav>
  )
}
