/** Subconjunto de org.springframework.data.domain.Page que usa el frontend. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
