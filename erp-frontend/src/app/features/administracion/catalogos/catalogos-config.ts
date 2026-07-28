/**
 * Configuracion declarativa de los catalogos simples de administracion.
 * Todos comparten el mismo patron de API: GET lista completa, POST, PUT /{id}, DELETE /{id}.
 * Agregar un catalogo nuevo = agregar una entrada aca (cero componentes nuevos).
 */
export interface CampoCatalogo {
  /** Clave del campo en el request (y en el response, para editar). */
  key: string;
  label: string;
  tipo: 'texto' | 'select' | 'checkbox';
  requerido?: boolean;
  maxLength?: number;
  /** Solo para tipo select: endpoint que devuelve las opciones (List). */
  opcionesUrl?: string;
  opcionValue?: string;
  opcionLabel?: string;
  /** Campo del response a mostrar en la tabla (p.ej. el nombre de la FK). */
  displayKey?: string;
}

export interface CatalogoConfig {
  /** Segmento de la ruta /admin/catalogos/:id */
  id: string;
  titulo: string;
  /** Sufijo del endpoint, p.ej. '/departamentos'. */
  endpoint: string;
  /** pagina_url para permisos (debe existir en la tabla opcion). */
  paginaUrl: string;
  /** Clave del id en el response, p.ej. 'idDepartamento'. */
  idKey: string;
  campos: CampoCatalogo[];
}

export const CATALOGOS: CatalogoConfig[] = [
  {
    id: 'departamentos', titulo: 'Departamentos', endpoint: '/departamentos',
    paginaUrl: '/api/departamentos', idKey: 'idDepartamento',
    campos: [{ key: 'nombreDepartamento', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 100 }],
  },
  {
    id: 'municipios', titulo: 'Municipios', endpoint: '/municipios',
    paginaUrl: '/api/municipios', idKey: 'idMunicipio',
    campos: [
      {
        key: 'idDepartamento', label: 'Departamento', tipo: 'select', requerido: true,
        opcionesUrl: '/departamentos', opcionValue: 'idDepartamento', opcionLabel: 'nombreDepartamento',
        displayKey: 'nombreDepartamento',
      },
      { key: 'nombreMunicipio', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 100 },
    ],
  },
  {
    id: 'tipos-estado', titulo: 'Tipos de estado', endpoint: '/tipos-estado',
    paginaUrl: '/api/tipos-estado', idKey: 'idTipoEstado',
    campos: [{ key: 'nombreTipo', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 50 }],
  },
  {
    id: 'estados', titulo: 'Estados', endpoint: '/estados',
    paginaUrl: '/api/estados', idKey: 'idEstado',
    campos: [
      {
        key: 'idTipoEstado', label: 'Tipo de estado', tipo: 'select', requerido: true,
        opcionesUrl: '/tipos-estado', opcionValue: 'idTipoEstado', opcionLabel: 'nombreTipo',
        displayKey: 'tipoEstadoNombre',
      },
      { key: 'nombre', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 50 },
    ],
  },
  {
    id: 'generos', titulo: 'Generos', endpoint: '/generos',
    paginaUrl: '/api/generos', idKey: 'idGenero',
    campos: [{ key: 'nombreGenero', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 30 }],
  },
  {
    id: 'tipos-documento', titulo: 'Tipos de documento', endpoint: '/tipos-documento',
    paginaUrl: '/api/tipos-documento', idKey: 'idTipoDocumento',
    campos: [{ key: 'nombreTipo', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 50 }],
  },
  {
    id: 'tipos-evento', titulo: 'Tipos de evento', endpoint: '/tipos-evento',
    paginaUrl: '/api/tipos-evento', idKey: 'idTipoEvento',
    campos: [{ key: 'nombreTipo', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 80 }],
  },
  {
    id: 'tipos-costo', titulo: 'Tipos de costo', endpoint: '/tipos-costo',
    paginaUrl: '/api/tipos-costo', idKey: 'idTipoCosto',
    campos: [
      { key: 'nombreTipo', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 80 },
      { key: 'descripcion', label: 'Descripcion', tipo: 'texto', maxLength: 255 },
    ],
  },
  {
    id: 'puestos-empleado', titulo: 'Puestos de empleado', endpoint: '/puestos-empleado',
    paginaUrl: '/api/puestos-empleado', idKey: 'idPuestoEmpleado',
    campos: [{ key: 'nombreRol', label: 'Nombre del puesto', tipo: 'texto', requerido: true, maxLength: 80 }],
  },
  {
    id: 'marcas-vehiculo', titulo: 'Marcas de vehiculo', endpoint: '/marcas-vehiculo',
    paginaUrl: '/api/marcas-vehiculo', idKey: 'idMarcaVehiculo',
    campos: [{ key: 'nombreMarca', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 60 }],
  },
  {
    id: 'lineas-vehiculo', titulo: 'Lineas de vehiculo', endpoint: '/lineas-vehiculo',
    paginaUrl: '/api/lineas-vehiculo', idKey: 'idLineaVehiculo',
    campos: [
      {
        key: 'idMarcaVehiculo', label: 'Marca', tipo: 'select', requerido: true,
        opcionesUrl: '/marcas-vehiculo', opcionValue: 'idMarcaVehiculo', opcionLabel: 'nombreMarca',
        displayKey: 'nombreMarca',
      },
      { key: 'nombreLinea', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 60 },
    ],
  },
  {
    id: 'tipos-placa', titulo: 'Tipos de placa', endpoint: '/tipos-placa',
    paginaUrl: '/api/tipos-placa', idKey: 'idTipoPlaca',
    campos: [{ key: 'nombreTipo', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 50 }],
  },
  {
    id: 'tipos-inventario', titulo: 'Tipos de inventario', endpoint: '/tipos-inventario',
    paginaUrl: '/api/tipos-inventario', idKey: 'idTipoInventario',
    campos: [
      { key: 'nombreTipo', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 80 },
      { key: 'descripcion', label: 'Descripcion', tipo: 'texto', maxLength: 255 },
    ],
  },
  {
    id: 'categorias-producto', titulo: 'Categorias de producto', endpoint: '/categorias-producto',
    paginaUrl: '/api/categorias-producto', idKey: 'idCategoria',
    campos: [
      {
        key: 'idTipoInventario', label: 'Tipo de inventario', tipo: 'select', requerido: true,
        opcionesUrl: '/tipos-inventario', opcionValue: 'idTipoInventario', opcionLabel: 'nombreTipo',
        displayKey: 'tipoInventarioNombre',
      },
      { key: 'nombreCategoria', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 80 },
    ],
  },
  {
    id: 'metodos-pago', titulo: 'Metodos de pago', endpoint: '/metodos-pago',
    paginaUrl: '/api/metodos-pago', idKey: 'idMetodoPago',
    campos: [
      { key: 'nombreMetodo', label: 'Nombre', tipo: 'texto', requerido: true, maxLength: 60 },
      { key: 'descripcion', label: 'Descripcion', tipo: 'texto', maxLength: 255 },
      {
        key: 'idEstado', label: 'Estado', tipo: 'select', requerido: true,
        opcionesUrl: '/estados', opcionValue: 'idEstado', opcionLabel: 'nombre',
        displayKey: 'estadoNombre',
      },
      { key: 'requiereReferencia', label: 'Requiere referencia', tipo: 'checkbox' },
    ],
  },
];
