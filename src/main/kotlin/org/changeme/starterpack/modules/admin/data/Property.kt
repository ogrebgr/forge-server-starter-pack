package org.changeme.starterpack.modules.admin.data

import com.bolyartech.forge.server.db.setValue
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import javax.inject.Inject


data class Property(
    val name: String,
    val value: String
)

interface PropertyDbh {
    @Throws(SQLException::class)
    fun save(dbc: Connection, name: String, value: String)

    @Throws(SQLException::class)
    fun loadByName(dbc: Connection, name: String): Property?

    @Throws(SQLException::class)
    fun loadAll(dbc: Connection): List<Property>

    @Throws(SQLException::class)
    fun delete(dbc: Connection, id: Int): Int

    @Throws(SQLException::class)
    fun deleteAll(dbc: Connection): Int
}

class PropertyDbhImpl @Inject constructor() : PropertyDbh {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    companion object {
        private const val SQL_INSERT = """INSERT INTO "properties" ("name", "value") VALUES (?, ?)"""
        private const val SQL_SELECT_BY_NAME = """SELECT "name", "value" FROM "properties" WHERE name = ?"""
        private const val SQL_SELECT_ALL = """SELECT "name", "value" FROM "properties""""
        private const val SQL_UPDATE = """UPDATE "properties" SET value = ? WHERE name = ?"""
        private const val SQL_DELETE = """DELETE FROM "properties" WHERE id = ?"""
        private const val SQL_DELETE_ALL = """DELETE FROM "properties""""
    }

    @Throws(SQLException::class)
    private fun createNew(dbc: Connection, name: String, value: String): Property {
        dbc.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS).use {
            it.setValue(1, name)
            it.setValue(2, value)

            it.executeUpdate()
            it.generatedKeys.use {
                it.next()
                return Property(
                    name,
                    value
                )
            }
        }
    }

    override fun save(dbc: Connection, name: String, value: String) {
        try {
            dbc.autoCommit = false
            dbc.transactionIsolation = Connection.TRANSACTION_SERIALIZABLE

            val p = loadByName(dbc, name)
            if (p == null) {
                createNew(dbc, name, value)
            } else {
                update(dbc, name, value)
            }

            dbc.commit()
        } catch (e: Throwable) {
            dbc.rollback()
            logger.error("Exception: ", e)
            throw e
        } finally {
            dbc.autoCommit = true
            dbc.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
        }
    }

    @Throws(SQLException::class)
    override fun loadByName(dbc: Connection, name: String): Property? {
        dbc.prepareStatement(SQL_SELECT_BY_NAME).use {
            it.setValue(1, name)

            it.executeQuery().use {
                return if (it.next()) {
                    Property(
                        it.getString(1),
                        it.getString(2)
                    )
                } else {
                    null
                }
            }
        }
    }

    @Throws(SQLException::class)
    override fun loadAll(dbc: Connection): List<Property> {
        val ret = ArrayList<Property>()
        dbc.prepareStatement(SQL_SELECT_ALL).use {
            it.executeQuery().use {
                while (it.next()) {
                    ret.add(
                        Property(
                            it.getString(1),
                            it.getString(2)
                        )
                    )
                }
            }
        }

        return ret
    }

    @Throws(SQLException::class)
    private fun update(dbc: Connection, name: String, value: String): Boolean {
        dbc.prepareStatement(SQL_UPDATE).use {
            it.setValue(1, value)
            it.setValue(2, name)

            return it.executeUpdate() > 0
        }
    }

    @Throws(SQLException::class)
    override fun delete(dbc: Connection, id: Int): Int {
        dbc.prepareStatement(SQL_DELETE).use {
            it.setValue(1, id)
            return it.executeUpdate()
        }
    }

    @Throws(SQLException::class)
    override fun deleteAll(dbc: Connection): Int {
        dbc.prepareStatement(SQL_DELETE_ALL).use {
            return it.executeUpdate()
        }
    }
}