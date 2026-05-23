export interface SelectOption {
  label: string
  value: unknown
}

export interface ColumnDef {
  prop: string
  label: string
  width?: number | string
  slot?: string
  formatter?: (row: Record<string, unknown>) => string
}

export interface SearchField {
  key: string
  placeholder?: string
  type: 'input' | 'select' | 'number' | 'date'
  options?: SelectOption[]
  min?: number
  max?: number
  clearable?: boolean
  filterable?: boolean
  width?: string
}

export interface FormField {
  key: string
  label: string
  type: 'input' | 'select' | 'number' | 'date' | 'radio' | 'textarea'
  inputType?: 'text' | 'password'
  required?: boolean
  placeholder?: string
  options?: SelectOption[]
  min?: number
  max?: number
  step?: number
  precision?: number
  rows?: number
  valueFormat?: string
  filterable?: boolean
  hideOnEdit?: boolean
}
