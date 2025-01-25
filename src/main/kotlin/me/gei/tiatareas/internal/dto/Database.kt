package me.gei.tiatareas.internal.dto

import javax.sql.DataSource

class Database(private val type: Type, private val dataSource: DataSource = type.host().createDataSource()) {

    init {
        type.tableVar().createTable(dataSource)
    }

    /**
     * 是否存在用户
     */
    private fun hasUser(user: String): Boolean {
        var result: String? = null
        type.tableVar().select(dataSource) {
            rows("user")
            where("user" eq user)
        }.firstOrNull {
            result = getString("user")
        }

        return result != null
    }

    /**
     *  根据用户获取用户所有的区域发现键值对
     */
    fun getDiscovering(user: String): String? {
        return type.tableVar().select(dataSource) {
            rows("discovering")
            where("user" eq user)
        }.firstOrNull {
            getString("discovering")
        }
    }

    /**
     *  根据用户获取用户所有的区域发现键值对
     */
    fun getDiscovered(user: String): String? {
        return type.tableVar().select(dataSource) {
            rows("discovered")
            where("user" eq user)
        }.firstOrNull {
            getString("discovered")
        }
    }

    /**
     *  设置用户域发现键值对
     *  如果数据为空则转为删除操作
     */
    fun setDiscovering(user: String, discovering: String?) {
        if (discovering == null) {
            removeDiscovering(user)
            return
        }
        if (!hasUser(user)) {
            type.tableVar().insert(dataSource, "user", "discovering") {
                value(user, discovering)
            }
        } else {
            type.tableVar().update(dataSource) {
                set("discovering", discovering)
                where("user" eq user)
            }
        }
    }

    /**
     *  设置用户域发现键值对
     *  如果数据为空则转为删除操作
     */
    fun setDiscovered(user: String, discovered: String?) {
        if (discovered == null) {
            removeDiscovered(user)
            return
        }
        if (!hasUser(user)) {
            type.tableVar().insert(dataSource, "user", "discovered") {
                value(user, discovered)
            }
        } else {
            type.tableVar().update(dataSource) {
                set("discovered", discovered)
                where("user" eq user)
            }
        }
    }

    /**
     *  删除
     */
    private fun removeDiscovering(user: String) {
        type.tableVar().update(dataSource) {
            set("discovering", null)
            where("user" eq user)
        }
    }

    /**
     * 删除
     */
    private fun removeDiscovered(user: String) {
        type.tableVar().update(dataSource) {
            set("discovered", null)
            where("user" eq user)
        }
    }
}