import { randomUUID } from 'node:crypto';
import { expect, type Browser, type Page } from '@playwright/test';
import { KEYCLOAK_URL } from './entorno';
import { esperarEnlace } from './mailpit';
import { crearPkce } from './pkce';

export type Tokens = { accessToken: string; refreshToken: string; idToken: string };

export const REALM_URL = `${KEYCLOAK_URL}/realms/trayecta-go`;
export const ESQUEMA_APP = 'co.trayectago.app:';
const REDIRECT = 'co.trayectago.app:/oauth2redirect';
const CLIENTE = 'app-movil';

/** Correo único por prueba: el enlace mágico crea la cuenta en el primer ingreso. */
export const correoNuevo = () => `prueba-${randomUUID()}@trayecta.test`;

export function decodificarJwt(token: string): Record<string, unknown> {
  return JSON.parse(Buffer.from(token.split('.')[1], 'base64url').toString('utf8'));
}

/** URL de autorización igual a la que arma AppAuth (PKCE S256, offline_access). */
export function urlDeAutorizacion(desafio: string, state: string, correo?: string): string {
  const q = new URLSearchParams({
    client_id: CLIENTE,
    response_type: 'code',
    scope: 'openid offline_access',
    redirect_uri: REDIRECT,
    code_challenge: desafio,
    code_challenge_method: 'S256',
    state,
  });
  if (correo) q.set('login_hint', correo);
  return `${REALM_URL}/protocol/openid-connect/auth?${q}`;
}

/** Resuelve con la URL de regreso a la app: la respuesta 3xx de Keycloak o la petición al esquema propio. */
export function esperarRedireccionApp(pagina: Page): Promise<URL> {
  return new Promise((resolver) => {
    pagina.on('response', (r) => {
      const destino = r.headers()['location'];
      if (r.status() >= 300 && r.status() < 400 && destino?.startsWith(ESQUEMA_APP)) resolver(new URL(destino));
    });
    pagina.on('request', (p) => {
      if (p.url().startsWith(ESQUEMA_APP)) resolver(new URL(p.url()));
    });
  });
}

export type SolicitudEnlace = { pagina: Page; verificador: string; state: string; desde: Date };

/** Abre el ingreso con login_hint, confirma que el correo llegó prellenado y pide el enlace. */
export async function pedirEnlace(browser: Browser, correo: string): Promise<SolicitudEnlace> {
  const { verificador, desafio } = crearPkce();
  const state = randomUUID();
  const pagina = await (await browser.newContext()).newPage();
  const desde = new Date();
  await pagina.goto(urlDeAutorizacion(desafio, state, correo));
  const campo = pagina.locator('input[name="username"]');
  await expect(campo).toHaveValue(correo);
  await campo.press('Enter');
  return { pagina, verificador, state, desde };
}

/** Abre el enlace del correo en otro contexto, como si fuera Gmail u otro navegador. */
export async function abrirEnlaceEnOtroNavegador(browser: Browser, correo: string, desde: Date): Promise<void> {
  const enlace = await esperarEnlace(correo, desde);
  const otra = await (await browser.newContext()).newPage();
  await otra.goto(enlace);
}

/** Flujo completo: pedir el enlace, abrirlo en otro navegador, tocar Continuar y cambiar el código por tokens. */
export async function iniciarSesionConEnlace(browser: Browser, correo: string): Promise<Tokens> {
  const { pagina, verificador, state, desde } = await pedirEnlace(browser, correo);
  const redireccion = esperarRedireccionApp(pagina);
  await abrirEnlaceEnOtroNavegador(browser, correo, desde);

  const continuar = pagina.locator('#mlc-continue');
  await expect(continuar).toBeVisible({ timeout: 20_000 });
  await continuar.click({ noWaitAfter: true });

  const destino = await redireccion;
  expect(destino.searchParams.get('state')).toBe(state);
  const code = destino.searchParams.get('code');
  if (!code) throw new Error(`Keycloak no devolvió code: ${destino}`);

  const r = await fetch(`${REALM_URL}/protocol/openid-connect/token`, {
    method: 'POST',
    headers: { 'content-type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'authorization_code',
      client_id: CLIENTE,
      code,
      redirect_uri: REDIRECT,
      code_verifier: verificador,
    }),
  });
  if (!r.ok) throw new Error(`El endpoint de token respondió ${r.status}: ${await r.text()}`);
  const j = (await r.json()) as { access_token: string; refresh_token: string; id_token: string };
  return { accessToken: j.access_token, refreshToken: j.refresh_token, idToken: j.id_token };
}
