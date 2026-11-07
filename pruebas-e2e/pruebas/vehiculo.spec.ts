import { expect, test } from '@playwright/test';
import { API_URL } from '../utilidades/entorno';
import { correoNuevo, iniciarSesionConEnlace } from '../utilidades/sesion';

test('el usuario guarda y consulta su moto', async ({ browser }) => {
  const sesion = await iniciarSesionConEnlace(browser, correoNuevo());
  const headers = { authorization: `Bearer ${sesion.accessToken}`, 'content-type': 'application/json' };
  const vehiculo = { tipo: 'moto', ultimoDigitoPlaca: 4, llevaParrillero: false };

  const guardado = await fetch(`${API_URL}/v1/me/vehiculo`, { method: 'PUT', headers, body: JSON.stringify(vehiculo) });
  expect(guardado.status).toBe(200);
  expect(await guardado.json()).toEqual(vehiculo);

  const consultado = await fetch(`${API_URL}/v1/me/vehiculo`, { headers });
  expect(consultado.status).toBe(200);
  expect(await consultado.json()).toEqual(vehiculo);
});
