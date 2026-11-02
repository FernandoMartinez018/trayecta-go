import { createHash, randomBytes } from 'node:crypto';

/** Par PKCE S256 como el que arma AppAuth en la app. */
export function crearPkce(): { verificador: string; desafio: string } {
  const verificador = randomBytes(32).toString('base64url');
  const desafio = createHash('sha256').update(verificador).digest('base64url');
  return { verificador, desafio };
}
