web: target/universal/stage/bin/play-getting-started -Dhttp.port=${PORT}  -Dapplication.global=Global  -Dplay.evolutions.db.default.autoApply=true -Ddb.default.driver=org.postgresql.Driver -Ddb.default.url=${DATABASE_URL}
worker: target/universal/stage/bin/play-getting-started  -Dapplication.global=Worker  -Dplay.evolutions.db.default.autoApply=true -Ddb.default.driver=org.postgresql.Driver -Ddb.default.url=${DATABASE_URL}
console: target/universal/stage/bin/play-getting-started -main scala.tools.nsc.MainGenericRunner -usejavacp
