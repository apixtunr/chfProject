// El backend corre en la misma maquina que sirve el frontend, en el puerto 8080. Se toma
// el nombre del equipo desde la barra de direcciones en vez de fijar "localhost": asi el
// sistema funciona igual en la computadora (localhost) y desde un celular de la misma red
// (192.168.x.x), donde "localhost" seria el propio celular.
export const API_URL = `${location.protocol}//${location.hostname}:8080/api`;
