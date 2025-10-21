package org.changeme.starterpack.modules.admin.data

import com.bolyartech.forge.server.db.setValue
import java.io.Serializable
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import javax.inject.Inject

data class AdminUser(
    val id: Int,
    val isDisabled: Boolean,
    val isSuperAdmin: Boolean,
    val name: String
) : Serializable {

    companion object {
        const val PASSWORD_MIN_LENGTH = 9

        fun isValidPasswordLength(len: Int): Boolean {
            return len >= PASSWORD_MIN_LENGTH
        }
    }
}

interface AdminUserDbh {
    @Throws(SQLException::class)
    fun createNew(dbc: Connection, is_disabled: Boolean, is_superadmin: Boolean, name: String): AdminUser

    @Throws(SQLException::class)
    fun loadById(dbc: Connection, id: Int): AdminUser?

    @Throws(SQLException::class)
    fun loadAll(dbc: Connection): List<AdminUser>

    @Throws(SQLException::class)
    fun update(dbc: Connection, obj: AdminUser): Boolean

    @Throws(SQLException::class)
    fun loadPageIdGreater(dbc: Connection, idGreater: Int, pageSize: Int): List<AdminUser>

    @Throws(SQLException::class)
    fun loadPageIdLower(dbc: Connection, idLower: Int, pageSize: Int): List<AdminUser>

    @Throws(SQLException::class)
    fun loadLastPage(dbc: Connection, pageSize: Int): List<AdminUser>

    @Throws(SQLException::class)
    fun count(dbc: Connection): Int

    @Throws(SQLException::class)
    fun delete(dbc: Connection, id: Int): Int

    @Throws(SQLException::class)
    fun deleteAll(dbc: Connection): Int

    @Throws(SQLException::class)
    fun changeIsSuperAdmin(dbc: Connection, userId: Int, isSuperAdmin: Boolean): Boolean

    @Throws(SQLException::class)
    fun changeIsDisabled(dbc: Connection, userId: Int, isDisabled: Boolean): Boolean

}

class AdminUserDbhImpl @Inject constructor() :
    AdminUserDbh {
    private val SQL_INSERT =
        """INSERT INTO "admin_users" ("is_disabled", "is_superadmin", "name") VALUES (?, ?, ?)"""
    private val SQL_SELECT_BY_ID =
        """SELECT "is_disabled", "is_superadmin", "name" FROM "admin_users" WHERE id = ?"""
    private val SQL_SELECT_ALL =
        """SELECT "id", "is_disabled", "is_superadmin", "name" FROM "admin_users""""
    private val SQL_UPDATE =
        """UPDATE "admin_users" SET "is_disabled" = ?, "is_superadmin" = ?, "name" = ? WHERE id = ?"""
    private val SQL_SELECT_ID_GREATER =
        """SELECT "id", "is_disabled", "is_superadmin", "name" FROM "admin_users" WHERE id > ?"""
    private val SQL_SELECT_ID_LOWER =
        """SELECT "id", "is_disabled", "is_superadmin", "name" FROM "admin_users" WHERE id < ?"""
    private val SQL_SELECT_LAST =
        """SELECT "id", "is_disabled", "is_superadmin", "name" FROM "admin_users" ORDER BY id DESC"""
    private val SQL_COUNT = """SELECT COUNT(id) FROM "admin_users""""
    private val SQL_DELETE = """DELETE FROM "admin_users" WHERE id = ?"""
    private val SQL_DELETE_ALL = """DELETE FROM "admin_users""""
    private val SQL_CHANGE_SUPER_ADMIN =
        """UPDATE "admin_users" SET "is_superadmin" = ? WHERE id = ?"""
    private val SQL_CHANGE_DISABLED =
        """UPDATE "admin_users" SET "is_disabled" = ? WHERE id = ?"""


    @Throws(SQLException::class)
    override fun createNew(dbc: Connection, is_disabled: Boolean, is_superadmin: Boolean, name: String): AdminUser {
        dbc.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS).use {
            it.setValue(1, is_disabled)
            it.setValue(2, is_superadmin)
            it.setValue(3, name)

            it.executeUpdate()
            it.generatedKeys.use {
                it.next()
                return AdminUser(
                    it.getInt(1),
                    is_disabled,
                    is_superadmin,
                    name
                )
            }
        }
    }


    @Throws(SQLException::class)
    override fun loadById(dbc: Connection, id: Int): AdminUser? {
        dbc.prepareStatement(SQL_SELECT_BY_ID).use {
            it.setValue(1, id)

            it.executeQuery().use {
                return if (it.next()) {
                    AdminUser(
                        id,
                        it.getBoolean(1),
                        it.getBoolean(2),
                        it.getString(3)
                    )
                } else {
                    null
                }
            }
        }
    }

    @Throws(SQLException::class)
    override fun loadAll(dbc: Connection): List<AdminUser> {
        val ret = ArrayList<AdminUser>()
        dbc.prepareStatement(SQL_SELECT_ALL).use {
            it.executeQuery().use {
                while (it.next()) {
                    ret.add(
                        AdminUser(
                            it.getInt(1),
                            it.getBoolean(2),
                            it.getBoolean(3),
                            it.getString(4)
                        )
                    )
                }
            }
        }

        return ret
    }

    @Throws(SQLException::class)
    override fun update(dbc: Connection, obj: AdminUser): Boolean {
        dbc.prepareStatement(SQL_UPDATE).use {
            it.setValue(1, obj.isDisabled)
            it.setValue(2, obj.isSuperAdmin)
            it.setValue(3, obj.name)
            it.setValue(4, obj.id)

            return it.executeUpdate() > 0
        }
    }

    @Throws(SQLException::class)
    override fun loadPageIdGreater(dbc: Connection, idGreater: Int, pageSize: Int): List<AdminUser> {
        if (pageSize <= 0) {
            throw IllegalArgumentException("pageSize <= 0")
        }

        dbc.prepareStatement(SQL_SELECT_ID_GREATER).use {
            it.setValue(1, idGreater)

            it.executeQuery().use {
                var count = 0
                val ret = ArrayList<AdminUser>()

                while (it.next()) {
                    ret.add(
                        AdminUser(
                            it.getInt(1),
                            it.getBoolean(2),
                            it.getBoolean(3),
                            it.getString(4)
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
    override fun loadPageIdLower(dbc: Connection, idLower: Int, pageSize: Int): List<AdminUser> {
        if (pageSize <= 0) {
            throw IllegalArgumentException("pageSize <= 0")
        }

        dbc.prepareStatement(SQL_SELECT_ID_LOWER).use {
            it.setValue(1, idLower)

            it.executeQuery().use {
                var count = 0
                val ret = ArrayList<AdminUser>()

                while (it.next()) {
                    ret.add(
                        AdminUser(
                            it.getInt(1),
                            it.getBoolean(2),
                            it.getBoolean(3),
                            it.getString(4)
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
    override fun loadLastPage(dbc: Connection, pageSize: Int): List<AdminUser> {
        val ret = ArrayList<AdminUser>()
        dbc.prepareStatement(SQL_SELECT_LAST).use {
            it.executeQuery().use {
                var count = 0
                while (it.next()) {
                    ret.add(
                        AdminUser(
                            it.getInt(1),
                            it.getBoolean(2),
                            it.getBoolean(3),
                            it.getString(4)
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

    override fun changeIsSuperAdmin(dbc: Connection, userId: Int, isSuperAdmin: Boolean): Boolean {
        dbc.prepareStatement(SQL_CHANGE_SUPER_ADMIN).use {
            it.setValue(1, isSuperAdmin)
            it.setValue(2, userId)

            return it.executeUpdate() > 0
        }
    }

    override fun changeIsDisabled(dbc: Connection, userId: Int, isDisabled: Boolean): Boolean {
        dbc.prepareStatement(SQL_CHANGE_DISABLED).use {
            it.setValue(1, isDisabled)
            it.setValue(2, userId)

            return it.executeUpdate() > 0
        }
    }
}