import { KEYCLOAK_URL, MAILPIT_URL } from './entorno';

type Resumen = { ID: string; Created: string };

/** Espera el correo enviado a `correo` desde `desde` y devuelve su asunto y texto. */
export async function esperarMensaje(correo: string, desde: Date): Promise<{ asunto: string; texto: string }> {
  const limite = Date.now() + 30_000;
  // Margen por si los relojes de los contenedores difieren un poco.
  const minimo = desde.getTime() - 5_000;
  while (Date.now() < limite) {
    const r = await fetch(`${MAILPIT_URL}/api/v1/search?query=${encodeURIComponent(`to:"${correo}"`)}`);
    if (r.ok) {
      const { messages } = (await r.json()) as { messages: Resumen[] };
      const m = messages.find((x) => new Date(x.Created).getTime() >= minimo);
      if (m) {
        const d = (await (await fetch(`${MAILPIT_URL}/api/v1/message/${m.ID}`)).json()) as { Subject: string; Text: string };
        return { asunto: d.Subject, texto: d.Text };
      }
    }
    await new Promise((listo) => setTimeout(listo, 500));
  }
  throw new Error(`No llegó ningún correo a ${correo} en 30 s`);
}

/** Espera el correo y extrae el enlace de ingreso de Keycloak. */
export async function esperarEnlace(correo: string, desde: Date): Promise<string> {
  const { texto } = await esperarMensaje(correo, desde);
  const prefijo = `${KEYCLOAK_URL}/realms/trayecta-go/login-actions/action-token`;
  const enlace = texto
    .split(/\s+/)
    .map((p) => p.replaceAll('&amp;', '&'))
    .find((p) => p.startsWith(prefijo));
  if (!enlace) throw new Error(`El correo a ${correo} no trae el enlace de ingreso`);
  return enlace;
}
