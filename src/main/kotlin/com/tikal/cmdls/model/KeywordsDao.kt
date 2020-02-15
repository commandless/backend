package com.tikal.cmdls.model

import io.reactivex.Flowable
import io.reactivex.Single
import io.vertx.reactivex.pgclient.PgPool
import io.vertx.reactivex.sqlclient.Row
import io.vertx.reactivex.sqlclient.Tuple
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.transaction.Transactional

@ApplicationScoped
class KeywordDao {

    @Inject
    lateinit var client: PgPool

    companion object {
        const val TABLE_NAME = "KEYWORDS"
        val CREATE_TABLE = """
            DROP TABLE IF EXISTS $TABLE_NAME;
            CREATE TABLE $TABLE_NAME (
                id SERIAL PRIMARY KEY, 
                keyword TEXT NOT NULL)
        """.trimIndent()
    }

    fun findAll(): Flowable<Keywords> =
            client.rxQuery("SELECT id, keyword FROM $TABLE_NAME")
                    .flatMapPublisher { rowSet ->
                        Flowable.fromIterable(rowSet.asIterable())

                    }
                    .map { row -> rowSetToKeyword(row) }

    private fun rowSetToKeyword(row: Row): Keywords =
            Keywords(row.getLong("id"), row.getString("keyword"))

    @Transactional
    fun add(keyword: String): Single<Long> =
            client.rxPreparedQuery("INSERT INTO $TABLE_NAME (keyword) VALUES ($1) RETURNING (id)", Tuple.of(keyword))
                    .flatMapPublisher { rowSet -> Flowable.fromIterable(rowSet.asIterable()) }
                    .map { it.getLong("id") }
                    .firstElement()
                    .toSingle()
}
