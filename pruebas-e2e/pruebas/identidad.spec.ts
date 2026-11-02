import { randomUUID } from 'node:crypto';
import { expect, test } from '@playwright/test';
import { KEYCLOAK_URL } from '../utilidades/entorno';
import { esperarMensaje } from '../utilidades/mailpit';
import { crearPkce } from '../utilidades/pkce';
import {
  abrirEnlaceEnOtroNavegador,
  decodificarJwt,
  ESQUEMA_APP,
  iniciarSesionConEnlace,
  pedirEnlace,
  urlDeAutorizacion,
} from '../utilidades/sesion';

const correoNuevo = () => `prueba-${randomUUID()}@trayecta.test`;

test('el enlace mágico entrega tokens para la app', async ({ browser }) => {
  const t = await iniciarSesionConEnlace(browser, correoNuevo());

  const acceso = decodificarJwt(t.accessToken);
  expect([acceso.aud].flat()).toContain('trayecta-go-api');
  expect(acceso.iss).toBe(`${KEYCLOAK_URL}/realms/trayecta-go`);
  expect(acceso.azp).toBe('app-movil');
  expect(typeof acceso.sub).toBe('string');
  expect((acceso.exp as number) - (acceso.iat as number)).toBe(300);
  expect(decodificarJwt(t.refreshToken).typ).toBe('Offline');
});

test('sin tocar Continuar no hay redirección a la app', async ({ browser }) => {
  const correo = correoNuevo();
  const { pagina, desde } = await pedirEnlace(browser, correo);
  let redirigio = false;
  pagina.on('request', (p) => {
    if (p.url().startsWith(ESQUEMA_APP)) redirigio = true;
  });
  pagina.on('response', (r) => {
    if (r.headers()['location']?.startsWith(ESQUEMA_APP)) redirigio = true;
  });

  await abrirEnlaceEnOtroNavegador(browser, correo, desde);

  await expect(pagina.locator('#mlc-continue')).toBeVisible({ timeout: 20_000 });
  await pagina.waitForTimeout(5_000);
  expect(redirigio).toBe(false);
});

test('la página de ingreso está en español y ofrece Google', async ({ page }) => {
  await page.goto(urlDeAutorizacion(crearPkce().desafio, randomUUID()));

  await expect(page.locator('html')).toHaveAttribute('lang', 'es');
  await expect(page.getByText('Google').first()).toBeVisible();
  await expect(page.locator('input[name="username"]')).toBeVisible();
});

test('el correo del enlace llega en español y con tuteo', async ({ browser }) => {
  const correo = correoNuevo();
  const { desde } = await pedirEnlace(browser, correo);

  const { asunto, texto } = await esperarMensaje(correo, desde);
  expect(asunto.startsWith('Tu enlace para entrar a')).toBe(true);
  expect(texto).toContain('Toca este enlace para entrar');
});
