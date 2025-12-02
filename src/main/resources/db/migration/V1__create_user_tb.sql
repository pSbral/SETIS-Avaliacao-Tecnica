CREATE TABLE IF NOT EXISTS tb_usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    data_nascimento DATE NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    data_edicao TIMESTAMP
);
