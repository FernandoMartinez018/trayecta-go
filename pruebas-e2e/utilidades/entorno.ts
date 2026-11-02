// URLs de los servicios; los valores por defecto son los del Compose local.
export const KEYCLOAK_URL = process.env.KEYCLOAK_URL ?? 'http://host.docker.internal:8081';
export const MAILPIT_URL = process.env.MAILPIT_URL ?? 'http://mailpit:8025';
export const API_URL = process.env.API_URL ?? 'http://backend:8080';
