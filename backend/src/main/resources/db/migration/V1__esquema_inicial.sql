-- Trayecta Go · esquema inicial
-- PostgreSQL 16 + PostGIS. Las celdas H3 se calculan en la aplicación y se guardan como texto.

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Usuarios y perfil ---------------------------------------------------------

CREATE TABLE usuario (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_id     text NOT NULL UNIQUE,
    alias_publico   text NOT NULL,
    preferencias    jsonb NOT NULL DEFAULT '{"manosLibres": true, "modoSeguro": false, "avisosLluvia": true, "anticipacionAvisoMin": 5}',
    creado_en       timestamptz NOT NULL DEFAULT now(),
    eliminar_en     timestamptz,
    eliminado_en    timestamptz
);

CREATE TABLE vehiculo (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id           uuid NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    tipo                 text NOT NULL CHECK (tipo IN ('moto')),
    ultimo_digito_placa  smallint NOT NULL CHECK (ultimo_digito_placa BETWEEN 0 AND 9),
    lleva_parrillero     boolean NOT NULL DEFAULT false,
    activo               boolean NOT NULL DEFAULT true,
    actualizado_en       timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX vehiculo_activo_por_usuario ON vehiculo (usuario_id) WHERE activo;

CREATE TABLE consentimiento (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id        uuid NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    tipo              text NOT NULL CHECK (tipo IN ('navegacion', 'aprender_rutas', 'aportar_trafico', 'comercial')),
    otorgado          boolean NOT NULL,
    version_politica  text NOT NULL,
    registrado_en     timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX consentimiento_usuario_tipo ON consentimiento (usuario_id, tipo, registrado_en DESC);

CREATE TABLE dispositivo (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id       uuid NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    plataforma       text NOT NULL CHECK (plataforma IN ('android', 'ios')),
    token_push_cifrado bytea NOT NULL,
    ultima_conexion  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE lugar_favorito (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id    uuid NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    nombre        text NOT NULL,
    ubicacion     geography(Point, 4326) NOT NULL,
    zona_privada  boolean NOT NULL DEFAULT true
);

-- Rutas y viajes ----------------------------------------------------------------

CREATE TABLE ruta (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id    uuid NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    vehiculo_id   uuid REFERENCES vehiculo(id) ON DELETE SET NULL,
    nombre        text,
    origen        geography(Point, 4326) NOT NULL,
    destino       geography(Point, 4326) NOT NULL,
    tipo          text NOT NULL CHECK (tipo IN ('programada', 'libre')),
    dias_semana   smallint[] NOT NULL DEFAULT '{}',
    hora_llegada  time,
    margen_min    smallint NOT NULL DEFAULT 5,
    activa        boolean NOT NULL DEFAULT true,
    creada_en     timestamptz NOT NULL DEFAULT now(),
    CHECK (tipo = 'libre' OR (hora_llegada IS NOT NULL AND cardinality(dias_semana) > 0))
);
CREATE INDEX ruta_programadas ON ruta (usuario_id) WHERE tipo = 'programada' AND activa;

CREATE TABLE viaje (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id          uuid NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    ruta_id             uuid REFERENCES ruta(id) ON DELETE SET NULL,
    salida_sugerida     timestamptz,
    inicio              timestamptz NOT NULL,
    llegada_predicha    timestamptz,
    llegada_real        timestamptz,
    valoracion          text CHECK (valoracion IN ('buena', 'mejorable')),
    geometria           geography(LineString, 4326),
    borrar_detalle_en   timestamptz NOT NULL DEFAULT (now() + interval '30 days'),
    anonimizar_en       timestamptz NOT NULL DEFAULT (now() + interval '12 months'),
    idempotency_key     uuid UNIQUE
);
CREATE INDEX viaje_retencion ON viaje (borrar_detalle_en) WHERE geometria IS NOT NULL;

CREATE TABLE parada (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    viaje_id   uuid NOT NULL REFERENCES viaje(id) ON DELETE CASCADE,
    orden      smallint NOT NULL,
    ubicacion  geography(Point, 4326) NOT NULL,
    estado     text NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'entregada'))
);

-- Alertas ----------------------------------------------------------------------

CREATE TABLE alerta (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo             text NOT NULL CHECK (tipo IN ('control_vial', 'accidente', 'hueco', 'via_inundada', 'via_cerrada', 'zona_riesgo')),
    ubicacion        geography(Point, 4326) NOT NULL,
    celda_h3         text NOT NULL,
    tramo_id         bigint,
    fuente           text NOT NULL CHECK (fuente IN ('comunidad', 'datos_abiertos', 'noticia', 'bot')),
    reportero_id     uuid REFERENCES usuario(id) ON DELETE SET NULL,
    confianza        numeric(4, 3) NOT NULL CHECK (confianza BETWEEN 0 AND 1),
    estado           text NOT NULL DEFAULT 'activa' CHECK (estado IN ('activa', 'vencida', 'descartada')),
    creada_en        timestamptz NOT NULL DEFAULT now(),
    vence_en         timestamptz NOT NULL,
    desvincular_en   timestamptz NOT NULL DEFAULT (now() + interval '12 months'),
    idempotency_key  uuid UNIQUE
);
CREATE INDEX alerta_activas_geo ON alerta USING gist (ubicacion) WHERE estado = 'activa';
CREATE INDEX alerta_activas_celda ON alerta (celda_h3) WHERE estado = 'activa';

CREATE TABLE confirmacion_alerta (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    alerta_id      uuid NOT NULL REFERENCES alerta(id) ON DELETE CASCADE,
    usuario_id     uuid REFERENCES usuario(id) ON DELETE SET NULL,
    tipo           text NOT NULL CHECK (tipo IN ('activa', 'pasiva')),
    sigue_ahi      boolean NOT NULL,
    registrado_en  timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX confirmacion_una_por_usuario ON confirmacion_alerta (alerta_id, usuario_id) WHERE tipo = 'activa';

CREATE TABLE reputacion (
    usuario_id  uuid PRIMARY KEY REFERENCES usuario(id) ON DELETE CASCADE,
    puntaje     numeric(5, 3) NOT NULL DEFAULT 0.5,
    puntos      integer NOT NULL DEFAULT 0,
    aciertos    integer NOT NULL DEFAULT 0,
    falsos      integer NOT NULL DEFAULT 0
);

-- Reglas locales -----------------------------------------------------------------

CREATE TABLE regla_local (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo            text NOT NULL CHECK (tipo IN ('pico_placa', 'parrillero', 'via_prohibida')),
    municipio       text NOT NULL,
    zona            geography(Geometry, 4326) NOT NULL,
    calendario      jsonb NOT NULL,
    vehiculos       text[] NOT NULL DEFAULT '{moto}',
    vigente_desde   date NOT NULL,
    vigente_hasta   date,
    fuente_url      text,
    version         integer NOT NULL,
    estado          text NOT NULL DEFAULT 'borrador' CHECK (estado IN ('borrador', 'aprobada', 'retirada')),
    aprobada_por    uuid REFERENCES usuario(id),
    aprobada_en     timestamptz
);
CREATE INDEX regla_local_vigentes ON regla_local USING gist (zona) WHERE estado = 'aprobada';

-- Agregados anónimos ---------------------------------------------------------------

CREATE TABLE riesgo_celda (
    celda_h3        text NOT NULL,
    franja_horaria  smallint NOT NULL CHECK (franja_horaria BETWEEN 0 AND 23),
    puntaje         numeric(4, 3) NOT NULL,
    actualizado_en  timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (celda_h3, franja_horaria)
);

CREATE TABLE velocidad_tramo (
    tramo_id        bigint NOT NULL,
    franja_horaria  smallint NOT NULL CHECK (franja_horaria BETWEEN 0 AND 23),
    dia_tipo        text NOT NULL CHECK (dia_tipo IN ('habil', 'sabado', 'festivo')),
    velocidad_kmh   numeric(5, 1) NOT NULL,
    muestras        integer NOT NULL,
    PRIMARY KEY (tramo_id, franja_horaria, dia_tipo)
);

-- Auditoría ----------------------------------------------------------------------------

CREATE TABLE auditoria (
    id             bigserial PRIMARY KEY,
    actor_id       uuid,
    accion         text NOT NULL,
    objeto         text NOT NULL,
    detalle        jsonb NOT NULL DEFAULT '{}',
    registrado_en  timestamptz NOT NULL DEFAULT now()
);
