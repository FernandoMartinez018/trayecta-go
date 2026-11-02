import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './pruebas',
  timeout: 120_000,
  // Las pruebas comparten Keycloak y Mailpit; en serie los correos no se cruzan.
  workers: 1,
  reporter: [['list']],
  use: {
    locale: 'es-CO',
    ignoreHTTPSErrors: process.env.IGNORAR_TLS === '1',
  },
});
