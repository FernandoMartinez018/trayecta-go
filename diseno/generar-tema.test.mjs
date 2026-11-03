import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { generar } from './generar-tema.mjs';

const tokens = JSON.parse(readFileSync(new URL('./tokens.json', import.meta.url), 'utf8'));
const conCambio = (cambio) => {
  const copia = structuredClone(tokens);
  cambio(copia.color.tokens.find((t) => t.name === 'brand'));
  return copia;
};
const lineaDeEstilo = (kotlin, nombre) => kotlin.split('\n').find((l) => l.trim().startsWith(`${nombre} = TextStyle(`));

test('genera brand de día y de noche', () => {
  const kotlin = generar(tokens);
  assert.ok(kotlin.includes('brand = Color(0xFF0F766E)'));
  assert.ok(kotlin.includes('brand = Color(0xFF2DD4BF)'));
});

test('convierte kebab a camelCase', () => {
  assert.ok(generar(tokens).includes('val brandPressed: Color'));
});

test('genera estilos con familia, tamaño, interlineado y peso', () => {
  assert.ok(
    generar(tokens).includes(
      'navDistance = TextStyle(fontFamily = display, fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight(800))',
    ),
  );
});

test('agrega letterSpacing solo cuando existe', () => {
  const kotlin = generar(tokens);
  assert.ok(lineaDeEstilo(kotlin, 'label').includes('letterSpacing = 0.4.sp'));
  assert.ok(!lineaDeEstilo(kotlin, 'body').includes('letterSpacing'));
});

test('falla si un color no tiene noche', () => {
  const sinNoche = conCambio((brand) => {
    brand.value = { light: '#0F766E' };
  });
  assert.throws(() => generar(sinNoche), /brand/);
});

test('falla con un hex inválido', () => {
  const hexMalo = conCambio((brand) => {
    brand.value.light = '#0F766';
  });
  assert.throws(() => generar(hexMalo), /#0F766/);
});

test('es determinista', () => {
  assert.equal(generar(tokens), generar(tokens));
});
