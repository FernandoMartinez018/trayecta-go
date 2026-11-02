import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { expect, test } from '@playwright/test';

type Ejecucion = { authenticator?: string; flowAlias?: string; requirement: string; authenticatorConfig?: string };
type Flujo = { alias: string; authenticationExecutions: Ejecucion[] };

// Se lee como texto plano: los marcadores ${VAR:default} se resuelven recién al importar.
const realm = JSON.parse(readFileSync(resolve(process.cwd(), '../identidad/realm/realm-trayecta-go.json'), 'utf8'));
const flujo = (alias: string): Flujo => realm.authenticationFlows.find((f: Flujo) => f.alias === alias);
const ejecucion = (f: Flujo, nombre: string) => f.authenticationExecutions.find((e) => e.authenticator === nombre);
const cliente = realm.clients.find((c: { clientId: string }) => c.clientId === 'app-movil');

test('el cliente app-movil es público, con PKCE S256 y sin flujos inseguros', () => {
  expect(cliente.publicClient).toBe(true);
  expect(cliente.attributes['pkce.code.challenge.method']).toBe('S256');
  expect(cliente.standardFlowEnabled).toBe(true);
  expect(cliente.implicitFlowEnabled).toBe(false);
  expect(cliente.directAccessGrantsEnabled).toBe(false);
  expect(cliente.redirectUris).toEqual(['co.trayectago.app:/oauth2redirect', 'co.trayectago.app.pruebas:/oauth2redirect']);
});

test('los tokens llevan la audiencia trayecta-go-api', () => {
  const mapper = cliente.protocolMappers.find((m: { protocolMapper: string }) => m.protocolMapper === 'oidc-audience-mapper');
  expect(mapper.config['included.custom.audience']).toBe('trayecta-go-api');
  expect(mapper.config['access.token.claim']).toBe('true');
});

test('los tiempos de sesión son los de la spec', () => {
  expect(realm.accessTokenLifespan).toBe(300);
  expect(realm.offlineSessionIdleTimeout).toBe(2592000);
});

test('el flujo de navegador ofrece cookie, Google y enlace mágico que crea usuarios', () => {
  expect(realm.browserFlow).toBe('trayecta-browser');
  const navegador = flujo('trayecta-browser');
  expect(ejecucion(navegador, 'auth-cookie')).toBeTruthy();
  expect(ejecucion(navegador, 'identity-provider-redirector')).toBeTruthy();
  const sub = navegador.authenticationExecutions.find((e) => e.flowAlias);
  const enlace = ejecucion(flujo(sub!.flowAlias!), 'magic-link-continuation-form');
  expect(enlace!.requirement).toBe('REQUIRED');
  const config = realm.authenticatorConfig.find((c: { alias: string }) => c.alias === enlace!.authenticatorConfig).config;
  expect(config['ext-magic-create-nonexistent-user']).toBe('true');
  expect(config['TIMEOUT']).toBe('10');
});

test('Google y el enlace mágico llevan a la misma cuenta', () => {
  const google = realm.identityProviders.find((p: { alias: string }) => p.alias === 'google');
  expect(google.trustEmail).toBe(true);
  expect(google.firstBrokerLoginFlowAlias).toBe('trayecta-primer-ingreso-google');
  const primerIngreso = flujo('trayecta-primer-ingreso-google');
  const sub = flujo(primerIngreso.authenticationExecutions.find((e) => e.flowAlias)!.flowAlias!);
  expect(ejecucion(sub, 'idp-create-user-if-unique')!.requirement).toBe('ALTERNATIVE');
  expect(ejecucion(sub, 'idp-auto-link')!.requirement).toBe('ALTERNATIVE');
});

test('sin registro, sin contraseñas y en español', () => {
  expect(realm.registrationAllowed).toBe(false);
  expect(realm.resetPasswordAllowed).toBe(false);
  expect(realm.loginWithEmailAllowed).toBe(true);
  expect(realm.registrationEmailAsUsername).toBe(true);
  expect(realm.duplicateEmailsAllowed).toBe(false);
  expect(realm.internationalizationEnabled).toBe(true);
  expect(realm.defaultLocale).toBe('es');
  expect(realm.loginTheme).toBe('trayecta');
});

test('no se declaran clientScopes ni requiredActions', () => {
  expect(realm).not.toHaveProperty('clientScopes');
  expect(realm).not.toHaveProperty('requiredActions');
});
