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
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    story TEXT,
    status_type TEXT,
    published BOOLEAN,
    page_id TEXT REFERENCES page(id) ON DELETE CASCADE
);

-- Tabla: reaction
CREATE TABLE IF NOT EXISTS reaction (
    id BIGSERIAL PRIMARY KEY,
    user_id TEXT,
    user_name TEXT,
    type TEXT,
    verb TEXT,
    comment_id TEXT,
    post_id TEXT REFERENCES post(id) ON DELETE CASCADE
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
    created_time TIMESTAMP,
    updated_time TIMESTAMP,
    verb TEXT,
    auto_answered BOOLEAN,
    agent_user TEXT,
    from_id TEXT REFERENCES fb_user(id),
    parent_id TEXT REFERENCES comment(id),
    post_id TEXT REFERENCES post(id) ON DELETE CASCADE,
    response_type TEXT
);

-- Tabla: attachment
CREATE TABLE IF NOT EXISTS attachment (
    id TEXT PRIMARY KEY,
    media_type TEXT,
    media_image TEXT,
    media_source TEXT,
    url TEXT,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    verb TEXT,
    message TEXT,
    post_id TEXT REFERENCES post(id) ON DELETE CASCADE
);
