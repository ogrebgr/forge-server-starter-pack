package org.changeme.starterpack.modules.admin.data

import java.sql.Connection
import java.sql.SQLException
import javax.inject.Inject

data class AdminUserExportedView(
    val id: Int,
    val username: String,
    val isDisabled: Boolean,
    val isSuperAdmin: Boolean,
    val name: String
)

interface AdminUserExportedViewDbh {
    @Throws(SQLException::class)
    fun list(
        dbc: Connection,
        idGreaterThan: Long,
        pageSize: Int
    ): List<AdminUserExportedView>

    @Throws(SQLException::class)
    fun findByPattern(
        dbc: Connection,
        pattern: String
    ): List<AdminUserExportedView>
}


class AdminUserExportedViewDbhImpl @Inject constructor() : AdminUserExportedViewDbh {
    override fun list(dbc: Connection, idGreaterThan: Long, pageSize: Int): List<AdminUserExportedView> {
        require(pageSize >= 0) { "Invalid limit: $pageSize" }

        val ret: MutableList<AdminUserExportedView> = ArrayList()


        val sql = "SELECT admin_users.id, admin_users.is_disabled, admin_users.is_superadmin, " +
                "admin_users.name, admin_user_blowfish.username " +
                "FROM admin_user_blowfish, admin_users " +
                "WHERE admin_users.id > ? AND admin_user_blowfish.user = admin_users.id LIMIT ?"

        dbc.prepareStatement(sql).use { st ->
            st.setLong(1, idGreaterThan)
            st.setLong(2, pageSize.toLong())
            val rs = st.executeQuery()
            while (rs.next()) {
                val tmp = AdminUserExportedView(
                    rs.getLong(1).toInt(),
                    rs.getString(5),
                    rs.getBoolean(2),
                    rs.getBoolean(3),
                    rs.getString(4)
                )
                ret.add(tmp)
            }
        }

        return ret
    }

    override fun findByPattern(dbc: Connection, pattern: String): List<AdminUserExportedView> {
        require(pattern.length >= 3) { "pattern must be at least 3 characters long" }

        val sql = "SELECT users.id, " +
                "admin_users.is_disabled, " +
                "admin_users.is_superadmin, " +
                "admin_users.name, " +
                "admin_user_blowfish.username " +
                "FROM admin_user_blowfish, admin_users " +
                "WHERE (admin_user_blowfish.username LIKE ? OR admin_users.name LIKE ?) " +
                "AND admin_user_blowfish.user = admin_users.id"

        val ret: MutableList<AdminUserExportedView> = ArrayList()

        dbc.prepareStatement(sql).use { st ->
            val sqlPattern = "$pattern%"
            st.setString(1, sqlPattern)
            st.setString(2, sqlPattern)
            val rs = st.executeQuery()
            while (rs.next()) {
                val tmp = AdminUserExportedView(
                    rs.getLong(1).toInt(),
                    rs.getString(5),
                    rs.getInt(2) == 1,
                    rs.getInt(3) == 1,
                    rs.getString(4)
                )
                ret.add(tmp)
            }
        }

        return ret
    }

}