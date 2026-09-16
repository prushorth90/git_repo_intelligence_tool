import type { ReactNode } from 'react'

export type TableColumn<Row> = {
  key: string
  header: string
  align?: 'left' | 'right'
  render: (row: Row) => ReactNode
}

type DataTableProps<Row> = {
  columns: TableColumn<Row>[]
  rows: Row[]
  getRowKey: (row: Row) => string | number
  emptyMessage?: string
}

export function DataTable<Row>({ columns, rows, getRowKey, emptyMessage = 'No records found' }: DataTableProps<Row>) {
  return (
    <div className="data-table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            {columns.map((column) => (
              <th className={column.align === 'right' ? 'align-right' : ''} key={column.key}>{column.header}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr><td className="table-empty" colSpan={columns.length}>{emptyMessage}</td></tr>
          ) : rows.map((row) => (
            <tr key={getRowKey(row)}>
              {columns.map((column) => (
                <td className={column.align === 'right' ? 'align-right' : ''} key={column.key}>{column.render(row)}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}