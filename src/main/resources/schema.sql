-- Tablas de negocio. Se ejecuta al arrancar (spring.sql.init.mode=always).
-- continue-on-error=true permite reejecutar si las tablas ya existen.

CREATE TABLE cuentas_anuales (
    id NUMBER(19) PRIMARY KEY,
    fecha DATE,
    transaccion VARCHAR2(50),
    monto NUMBER(19,2),
    descripcion VARCHAR2(255)
);

CREATE TABLE intereses (
    id NUMBER(19) PRIMARY KEY,
    nombre VARCHAR2(255),
    saldo NUMBER(19,2),
    edad NUMBER(10),
    tipo VARCHAR2(50)
);

CREATE TABLE transacciones (
    id NUMBER(19) PRIMARY KEY,
    fecha DATE,
    monto NUMBER(19,2),
    tipo VARCHAR2(50)
);
