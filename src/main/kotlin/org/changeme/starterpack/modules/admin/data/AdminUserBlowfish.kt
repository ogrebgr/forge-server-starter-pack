package org.changeme.starterpack.modules.admin.data

import com.bolyartech.forge.server.db.setValue
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import javax.inject.Inject


data class AdminUserBlowfish(
    val user: Int,
    val username: String,
    val password: String
)

interface AdminUserBlowfishDbh {
    @Throws(SQLException::class)
    fun createNew(dbc: Connection, user: Int, username: String, password: String): AdminUserBlowfish

    @Throws(SQLException::class)
    fun loadByUser(dbc: Connection, id: Int): AdminUserBlowfish?

    @Throws(SQLException::class)
    fun loadByUsername(dbc: Connection, username: String): AdminUserBlowfish?

    @Throws(SQLException::class)
    fun loadAll(dbc: Connection): List<AdminUserBlowfish>

    @Throws(SQLException::class)
    fun update(dbc: Connection, obj: AdminUserBlowfish): Boolean

    @Throws(SQLException::class)
    fun loadPageIdGreater(dbc: Connection, idGreater: Int, pageSize: Int): List<AdminUserBlowfish>

    @Throws(SQLException::class)
    fun loadPageIdLower(dbc: Connection, idLower: Int, pageSize: Int): List<AdminUserBlowfish>

    @Throws(SQLException::class)
    fun loadLastPage(dbc: Connection, pageSize: Int): List<AdminUserBlowfish>

    @Throws(SQLException::class)
    fun count(dbc: Connection): Int

    @Throws(SQLException::class)
    fun delete(dbc: Connection, id: Int): Int

    @Throws(SQLException::class)
    fun deleteAll(dbc: Connection): Int

    @Throws(SQLException::class)
    fun updatePassword(dbc: Connection, user: Int, passwordHash: String): Int
}

class AdminUserBlowfishDbhImpl @Inject constructor() : AdminUserBlowfishDbh {
    companion object {
        private const val SQL_INSERT = """INSERT INTO "admin_user_blowfish" ("user", "username", "password") VALUES (?, ?, ?)"""
        private const val SQL_SELECT_BY_USER = """SELECT "user", "username", "password" FROM "admin_user_blowfish" WHERE "user" = ?"""
        private const val SQL_SELECT_BY_USERNAME =
            """SELECT "user", "username", "password" FROM "admin_user_blowfish" WHERE username = ?"""
        private const val SQL_SELECT_ALL = """SELECT "user", "username", "password" FROM "admin_user_blowfish""""
        private const val SQL_UPDATE = """UPDATE "admin_user_blowfish" SET "user" = ?, "username" = ?, "password" = ? WHERE id = ?"""
        private const val SQL_SELECT_ID_GREATER = """SELECT "user", "username", "password" FROM "admin_user_blowfish" WHERE id > ?"""
        private const val SQL_SELECT_ID_LOWER = """SELECT "user", "username", "password" FROM "admin_user_blowfish" WHERE id < ?"""
        private const val SQL_SELECT_LAST = """SELECT "user", "username", "password" FROM "admin_user_blowfish" ORDER BY id DESC"""
        private const val SQL_COUNT = """SELECT COUNT(id) FROM "admin_user_blowfish""""
        private const val SQL_DELETE = """DELETE FROM "admin_user_blowfish" WHERE id = ?"""
        private const val SQL_DELETE_ALL = """DELETE FROM "admin_user_blowfish""""

        private const val SQL_UPDATE_PASSWORD = """ UPDATE "admin_user_blowfish" SET password = ? WHERE "user" = ? """
    }

    @Throws(SQLException::class)
    override fun createNew(dbc: Connection, user: Int, username: String, password: String): AdminUserBlowfish {
        dbc.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS).use {
            it.setValue(0, user)
            it.setValue(1, username)
            it.setValue(2, password)

            it.executeUpdate()
            it.generatedKeys.use {
                it.next()
                return AdminUserBlowfish(
                    user,
                    username,
                    password
                )
            }
        }
    }


    @Throws(SQLException::class)
    override fun loadByUser(dbc: Connection, id: Int): AdminUserBlowfish? {
        dbc.prepareStatement(SQL_SELECT_BY_USER).use {
            it.setValue(1, id)

            it.executeQuery().use {
                return if (it.next()) {
                    AdminUserBlowfish(
                        it.getInt(1),
                        it.getString(2),
                        it.getString(3)
                    )
                } else {
                    null
                }
            }
        }
    }

    override fun loadByUsername(dbc: Connection, username: String): AdminUserBlowfish? {
        dbc.prepareStatement(SQL_SELECT_BY_USERNAME).use {
            it.setValue(1, username)

            it.executeQuery().use {
                return if (it.next()) {
                    AdminUserBlowfish(
                        it.getInt(1),
                        it.getString(2),
                        it.getString(3)
                    )
                } else {
                    null
                }
            }
        }
    }

    @Throws(SQLException::class)
    override fun loadAll(dbc: Connection): List<AdminUserBlowfish> {
        val ret = mutableListOf<AdminUserBlowfish>()
        dbc.prepareStatement(SQL_SELECT_ALL).use {
            it.executeQuery().use {
                while (it.next()) {
                    ret.add(
                        AdminUserBlowfish(
                            it.getInt(1),
                            it.getString(2),
                            it.getString(3)
                        )
                    )
                }
            }
        }

        return ret
    }

    @Throws(SQLException::class)
    override fun update(dbc: Connection, obj: AdminUserBlowfish): Boolean {
        dbc.prepareStatement(SQL_UPDATE).use {
            it.setValue(1, obj.user)
            it.setValue(2, obj.username)
            it.setValue(3, obj.password)

            return it.executeUpdate() > 0
        }
    }

    @Throws(SQLException::class)
    override fun loadPageIdGreater(dbc: Connection, idGreater: Int, pageSize: Int): List<AdminUserBlowfish> {
        if (pageSize <= 0) {
            throw IllegalArgumentException("pageSize <= 0")
        }

        dbc.prepareStatement(SQL_SELECT_ID_GREATER).use {
            it.setValue(1, idGreater)

            it.executeQuery().use {
                var count = 0
                val ret = mutableListOf<AdminUserBlowfish>()

                while (it.next()) {
                    ret.add(
                        AdminUserBlowfish(
                            it.getInt(1),
                            it.getString(2),
                            it.getString(3)
                        )
                    )
                    count++
                    if (count == pageSize) {
                        return ret
                    }
                }

                return ret
            }
        }
    }


    @Throws(SQLException::class)
    override fun loadPageIdLower(dbc: Connection, idLower: Int, pageSize: Int): List<AdminUserBlowfish> {
        if (pageSize <= 0) {
            throw IllegalArgumentException("pageSize <= 0")
        }

        dbc.prepareStatement(SQL_SELECT_ID_LOWER).use {
            it.setValue(1, idLower)

            it.executeQuery().use {
                var count = 0
                val ret = mutableListOf<AdminUserBlowfish>()

                while (it.next()) {
                    ret.add(
                        AdminUserBlowfish(
                            it.getInt(1),
                            it.getString(2),
                            it.getString(3)
                        )
                    )
                    count++
                    if (count == pageSize) {
                        return ret
                    }
                }

                return ret
            }
        }
    }

    @Throws(SQLException::class)
    override fun loadLastPage(dbc: Connection, pageSize: Int): List<AdminUserBlowfish> {
        val ret = mutableListOf<AdminUserBlowfish>()
        dbc.prepareStatement(SQL_SELECT_LAST).use {
            it.executeQuery().use {
                var count = 0
                while (it.next()) {
                    ret.add(
                        AdminUserBlowfish(
                            it.getInt(1),
                            it.getString(2),
                            it.getString(3)
                        )
                    )
                    count++
                    if (count == pageSize) {
                        return ret
                    }
                }
            }
        }

        return ret
    }

    @Throws(SQLException::class)
    override fun count(dbc: Connection): Int {
        dbc.prepareStatement(SQL_COUNT).use {
            it.executeQuery().use {
                it.next()
                return it.getInt(1)
            }
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

    override fun updatePassword(dbc: Connection, user: Int, passwordHash: String): Int {
        dbc.prepareStatement(SQL_UPDATE_PASSWORD).use {
            it.setValue(1, passwordHash)
            it.setValue(2, user)

            return it.executeUpdate()
        }
    }
}