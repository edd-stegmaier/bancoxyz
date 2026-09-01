# bancoxyz

Spring Batch que carga CSV y genera resumenes. Runtime por defecto: **Oracle Autonomous Database** con wallet (mTLS). H2 queda disponible con el perfil `h2` y en tests.

## Conectar a Oracle Cloud (ajuste minimo)

1. En OCI: Autonomous Database -> **Database connection** -> **Download wallet**.
2. Descomprime el zip en un directorio **fuera del repo** (por ejemplo `~/oracle/wallet_bancoxyz`).
3. Abre `tnsnames.ora` y copia un alias (`*_high`, `*_medium` o `*_tp`).
4. Exporta las variables (o crea `src/main/resources/application-local.properties`, que esta en `.gitignore`):

```bash
export ORACLE_WALLET_DIR=/ruta/absoluta/al/wallet_descomprimido
export ORACLE_TNS_ALIAS=nombredb_high
export ORACLE_USERNAME=ADMIN
export ORACLE_PASSWORD='tu-password'
```

La URL JDBC queda:

```text
jdbc:oracle:thin:@${ORACLE_TNS_ALIAS}?TNS_ADMIN=${ORACLE_WALLET_DIR}
```

`TNS_ADMIN` debe ser la carpeta que contiene `tnsnames.ora`, `sqlnet.ora`, `cwallet.sso` y `ojdbc.properties`. En Windows usa `/` o `\\` en la ruta.

5. Arranca:

```bash
./mvnw spring-boot:run
```

Tras la primera ejecucion puedes poner `spring.batch.jdbc.initialize-schema=never` si las tablas `BATCH_*` ya existen.

## Seguir usando H2 en local

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

