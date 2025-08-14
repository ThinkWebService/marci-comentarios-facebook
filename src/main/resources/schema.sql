-- Tabla: page
CREATE TABLE IF NOT EXISTS page (
    id TEXT PRIMARY KEY,
    name TEXT,
    description TEXT,
    username TEXT,
    category TEXT,
    link TEXT,
    fan_count INT,
    followers_count INT,
    cover_url TEXT,
    profile_url TEXT,
    verified BOOLEAN
);

-- Tabla: post
CREATE TABLE IF NOT EXISTS post (
    id TEXT PRIMARY KEY,
    message TEXT,
    full_picture TEXT,
    permalink_url TEXT,
    created_time TIMESTAMP DEFAULT NOW(),
    updated_time TIMESTAMP DEFAULT NOW(),
    story TEXT,
    status_type TEXT,
    published BOOLEAN,
    page_id TEXT REFERENCES page(id) ON DELETE CASCADE,
    verb TEXT,
    auto_answered BOOLEAN DEFAULT FALSE
);

-- Tabla: reaction
CREATE TABLE IF NOT EXISTS reaction (
    id BIGSERIAL PRIMARY KEY,
    user_id TEXT,
    user_name TEXT,
    type TEXT,
    verb TEXT,
    comment_id TEXT,
    post_id TEXT REFERENCES post(id) ON DELETE CASCADE,
    created_time TIMESTAMP DEFAULT NOW()
);

-- Tabla: fb_user (equivalente a clase From)
CREATE TABLE IF NOT EXISTS fb_user (
    id TEXT PRIMARY KEY,
    name TEXT
);

-- Tabla: comment
CREATE TABLE IF NOT EXISTS comment (
    id TEXT PRIMARY KEY,
    message TEXT,
    created_time TIMESTAMP DEFAULT NOW(),
    updated_time TIMESTAMP DEFAULT NOW(),
    verb TEXT,
    auto_answered BOOLEAN,
    agent_user TEXT,
    from_id TEXT REFERENCES fb_user(id),
    parent_id TEXT REFERENCES comment(id),
    post_id TEXT REFERENCES post(id) ON DELETE CASCADE,
    response_type TEXT,
    previous_version_id TEXT REFERENCES comment(id)
    );

-- Tabla: attachment
CREATE TABLE IF NOT EXISTS attachment (
    id TEXT PRIMARY KEY,
    media_type TEXT,
    media_image TEXT,
    media_source TEXT,
    url TEXT,
    create_time TIMESTAMP DEFAULT NOW(),
    update_time TIMESTAMP DEFAULT NOW(),
    verb TEXT,
    message TEXT,
    post_id TEXT REFERENCES post(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS plantilla (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    descripcion TEXT,
    enlace TEXT,
    plantilla_type TEXT NOT NULL
);

CREATE OR REPLACE VIEW vista_resumen AS
SELECT
    p.id AS id_post,
    p.message AS mensaje,
    p.created_time AS creado,

    COUNT(DISTINCT c.id) AS comentarios,

    COUNT(DISTINCT CASE WHEN c.parent_id IS NULL THEN c.id END) AS comentarios_directos,
    COUNT(DISTINCT CASE WHEN c.parent_id IS NOT NULL THEN c.id END) AS respuestas,

    COUNT(DISTINCT CASE
                       WHEN c.parent_id IS NULL
                           AND EXISTS (
                               SELECT 1 FROM comment resp WHERE resp.parent_id = c.id
                           )
                           THEN c.id
        END) AS comentarios_respondidos,

    COUNT(DISTINCT CASE
                       WHEN c.parent_id IS NULL
                           AND NOT EXISTS (
                               SELECT 1 FROM comment resp WHERE resp.parent_id = c.id
                           )
                           THEN c.id
        END) AS comentarios_no_respondidos,

    COUNT(DISTINCT CASE WHEN c.auto_answered = TRUE THEN c.id END) AS auto_respondidos,

    COUNT(DISTINCT CASE
                       WHEN c.response_type IS NOT NULL AND c.auto_answered = FALSE THEN c.id
        END) AS agente_respondidos,

    -- Respuestas por tipo SOLO con parent_id (son respuestas a comentarios)
    COUNT(DISTINCT CASE
                       WHEN c.response_type = 'FACEBOOK' AND c.parent_id IS NOT NULL THEN c.id
        END) AS respuestas_facebook,

    COUNT(DISTINCT CASE
                       WHEN c.response_type = 'LHIA' AND c.parent_id IS NOT NULL THEN c.id
        END) AS respuestas_lhia,

    COUNT(DISTINCT CASE
                       WHEN c.response_type = 'SIMPLE' AND c.parent_id IS NOT NULL THEN c.id
        END) AS respuestas_simple,

    COUNT(DISTINCT CASE
                       WHEN c.response_type = 'PLANTILLA' AND c.parent_id IS NOT NULL THEN c.id
        END) AS respuestas_plantilla,

    COUNT(DISTINCT CASE
                       WHEN c.response_type = 'MEJORADA' AND c.parent_id IS NOT NULL THEN c.id
        END) AS respuestas_mejoradas,

    -- Segundos de respuesta (no negativos)
    GREATEST(
            (
                SELECT AVG(EXTRACT(EPOCH FROM (child.created_time - parent.created_time)))
                FROM comment child
                         JOIN comment parent ON child.parent_id = parent.id
                WHERE child.auto_answered = TRUE
                  AND parent.post_id = p.id
            ), 0
    ) AS segundos_respuesta_auto,

    GREATEST(
            (
                SELECT AVG(EXTRACT(EPOCH FROM (child.created_time - parent.created_time)))
                FROM comment child
                         JOIN comment parent ON child.parent_id = parent.id
                WHERE child.auto_answered = FALSE
                  AND child.response_type IS NOT NULL
                  AND parent.post_id = p.id
            ), 0
    ) AS segundos_respuesta_agente,

    COUNT(DISTINCT r.id) AS reacciones,

    (
        SELECT COUNT(DISTINCT u.id)
        FROM (
                 SELECT from_id AS id FROM comment WHERE post_id = p.id
                 UNION
                 SELECT user_id AS id FROM reaction WHERE post_id = p.id
             ) u
    ) AS usuarios,

    GREATEST(
            COALESCE(MAX(p.created_time), 'epoch'::timestamp),
            COALESCE(MAX(c.created_time), 'epoch'::timestamp),
            COALESCE(MAX(r.created_time), 'epoch'::timestamp)
    ) AS actualizado

FROM post p
         LEFT JOIN comment c ON c.post_id = p.id
         LEFT JOIN reaction r ON r.post_id = p.id
GROUP BY p.id, p.message, p.created_time;


-- Función genérica para notificar cambios en las tablas indicadas
CREATE OR REPLACE FUNCTION notify_post_update() RETURNS trigger AS $$
DECLARE
postId TEXT;
BEGIN
    IF TG_TABLE_NAME = 'post' THEN
        IF TG_OP = 'DELETE' THEN
            postId := OLD.id;
ELSE
            postId := NEW.id;
END IF;
    ELSIF TG_TABLE_NAME = 'comment' OR TG_TABLE_NAME = 'reaction' THEN
        IF TG_OP = 'DELETE' THEN
            postId := OLD.post_id;
ELSE
            postId := NEW.post_id;
END IF;
    ELSIF TG_TABLE_NAME = 'page' THEN
        IF TG_OP = 'DELETE' THEN
            postId := OLD.id; -- Puedes usar id de página si te interesa
ELSE
            postId := NEW.id;
END IF;
ELSE
        postId := NULL;
END IF;
    PERFORM pg_notify('post_update', COALESCE(postId, 'null'));

RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Triggers para las tablas que afectan la vista resumen

-- Comment
DROP TRIGGER IF EXISTS trg_comment_update ON comment;
CREATE TRIGGER trg_comment_update
    AFTER INSERT OR UPDATE OR DELETE ON comment
    FOR EACH ROW EXECUTE PROCEDURE notify_post_update();

-- Reaction
DROP TRIGGER IF EXISTS trg_reaction_update ON reaction;
CREATE TRIGGER trg_reaction_update
    AFTER INSERT OR UPDATE OR DELETE ON reaction
    FOR EACH ROW EXECUTE PROCEDURE notify_post_update();

-- Post
DROP TRIGGER IF EXISTS trg_post_update ON post;
CREATE TRIGGER trg_post_update
    AFTER INSERT OR UPDATE OR DELETE ON post
    FOR EACH ROW EXECUTE PROCEDURE notify_post_update();

-- Page
DROP TRIGGER IF EXISTS trg_page_update ON page;
CREATE TRIGGER trg_page_update
    AFTER INSERT OR UPDATE OR DELETE ON page
    FOR EACH ROW EXECUTE PROCEDURE notify_post_update();

