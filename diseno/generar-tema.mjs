// Genera el tema de Compose (TemaGenerado.kt) desde diseno/tokens.json. Sin dependencias: node diseno/generar-tema.mjs
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath, pathToFileURL } from 'node:url';

const SALIDA = new URL('../app/shared/src/commonMain/kotlin/co/trayectago/app/tema/TemaGenerado.kt', import.meta.url);
const TOKENS = new URL('./tokens.json', import.meta.url);
const SANGRIA = '    ';

/** brand-pressed → brandPressed, space-1 → space1. */
function camelCase(nombre) {
  return nombre
    .split('-')
    .map((parte, i) => (i === 0 ? parte : parte.charAt(0).toUpperCase() + parte.slice(1)))
    .join('');
}

function color(nombre, hex) {
  if (!/^#[0-9A-Fa-f]{6}$/.test(hex ?? '')) throw new Error(`Color inválido en '${nombre}': ${hex}`);
  return `Color(0xFF${hex.slice(1).toUpperCase()})`;
}

/** "4px" → "4", "0.4px" → "0.4". */
function pixeles(nombre, valor) {
  if (!/^\d+(\.\d+)?px$/.test(valor ?? '')) throw new Error(`Medida inválida en '${nombre}': ${valor}`);
  return String(Number(valor.slice(0, -2)));
}

function nombresUnicos(nombres, grupo) {
  const vistos = new Set();
  for (const n of nombres) {
    if (vistos.has(n)) throw new Error(`Nombre repetido en ${grupo}: ${n}`);
    vistos.add(n);
  }
}

function bloqueColores(colores, tema) {
  const campos = colores.map((t) => `${SANGRIA}${camelCase(t.name)} = ${color(t.name, t.value[tema])},`);
  return campos.join('\n');
}

function objetoDp(nombreObjeto, tokens) {
  nombresUnicos(tokens.map((t) => camelCase(t.name)), nombreObjeto);
  const lineas = tokens.map((t) => `${SANGRIA}val ${camelCase(t.name)} = ${pixeles(t.name, t.value)}.dp`);
  return `object ${nombreObjeto} {\n${lineas.join('\n')}\n}`;
}

function estilo(e, familia) {
  const partes = [
    `fontFamily = ${familia}`,
    `fontSize = ${pixeles(e.name, e.fontSize)}.sp`,
    `lineHeight = ${pixeles(e.name, e.lineHeight)}.sp`,
    `fontWeight = FontWeight(${Number(e.fontWeight)})`,
  ];
  if (e.letterSpacing !== undefined) partes.push(`letterSpacing = ${pixeles(e.name, e.letterSpacing)}.sp`);
  return `TextStyle(${partes.join(', ')})`;
}

export function generar(tokens) {
  const colores = tokens.color.tokens;
  for (const t of colores) {
    if (t.value?.light === undefined) throw new Error(`El color '${t.name}' no tiene valor de día (light)`);
    if (t.value?.dark === undefined) throw new Error(`El color '${t.name}' no tiene valor de noche (dark)`);
  }
  nombresUnicos(colores.map((t) => camelCase(t.name)), 'colores');

  const estilos = tokens.type.groups.flatMap((g) => {
    const familia = { display: 'display', text: 'texto' }[g.family];
    if (!familia) throw new Error(`Familia desconocida en el grupo '${g.name}': ${g.family}`);
    return g.styles.map((e) => ({ nombre: camelCase(e.name), codigo: estilo(e, familia) }));
  });
  nombresUnicos(estilos.map((e) => e.nombre), 'estilos de texto');

  return `// Generado por diseno/generar-tema.mjs desde diseno/tokens.json. No editar a mano.
package co.trayectago.app.tema

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class ColoresTrayecta(
${colores.map((t) => `${SANGRIA}val ${camelCase(t.name)}: Color,`).join('\n')}
)

val ColoresDia = ColoresTrayecta(
${bloqueColores(colores, 'light')}
)

val ColoresNoche = ColoresTrayecta(
${bloqueColores(colores, 'dark')}
)

${objetoDp('Espacios', tokens.spacing.tokens)}

${objetoDp('Radios', tokens.radius.tokens)}

${objetoDp('Tamanos', tokens.sizing.tokens)}

@Immutable
data class EstilosTexto(
${estilos.map((e) => `${SANGRIA}val ${e.nombre}: TextStyle,`).join('\n')}
)

fun estilosTexto(display: FontFamily, texto: FontFamily) = EstilosTexto(
${estilos.map((e) => `${SANGRIA}${e.nombre} = ${e.codigo},`).join('\n')}
)
`;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const kotlin = generar(JSON.parse(readFileSync(TOKENS, 'utf8')));
  writeFileSync(SALIDA, kotlin);
  console.log(`Escrito ${fileURLToPath(SALIDA)}`);
}
