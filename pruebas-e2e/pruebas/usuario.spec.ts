import { expect, test } from '@playwright/test';
import { API_URL } from '../utilidades/entorno';
import { correoNuevo, iniciarSesionConEnlace } from '../utilidades/sesion';

test('el usuario autenticado obtiene su perfil', async ({ browser }) => {
  const t = await iniciarSesionConEnlace(browser, correoNuevo());

  const r = await fetch(`${API_URL}/v1/me`, { headers: { authorization: `Bearer ${t.accessToken}` } });

  expect(r.status).toBe(200);
  const usuario = (await r.json()) as { alias: string; nivel: string };
  expect(usuario.alias).toMatch(/^Usuario \d{4}$/);
  expect(usuario.nivel).toBe('1');
});

test('sin token la API responde 401 en problem+json', async () => {
  const r = await fetch(`${API_URL}/v1/me`);

  expect(r.status).toBe(401);
  expect(r.headers.get('content-type')).toContain('application/problem+json');
});
