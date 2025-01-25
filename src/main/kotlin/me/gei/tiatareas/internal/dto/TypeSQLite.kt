package me.gei.tiatareas.internal.dto

import taboolib.common.io.newFile
import taboolib.common.platform.function.pluginId
import taboolib.module.database.*
import java.io.File

/**
 * SQLite 数据类型实现类。
 *
 * @property file 数据库文件
 * @property tableName 表名，如果为空则使用插件 ID
 */
class TypeSQLite(private val file: File, private val tableName: String? = null) : Type() {

    /**
     * SQLite 数据库主机
     */
    val host = newFile(file).getHost()

    /**
     * 数据表结构
     */
    private val tableVar = Table(tableName ?: pluginId, host) {
        add("user") {
            type(ColumnTypeSQLite.TEXT) {
                options(ColumnOptionSQLite.UNIQUE)
            }
        }
        add("discovering") {
            type(ColumnTypeSQLite.TEXT)
        }
        add("discovered") {
            type(ColumnTypeSQLite.TEXT)
        }
    }

    /**
     * 获取数据库主机
     *
     * @return 数据库主机
     */
    override fun host(): Host<*> {
        return host
    }

    /**
     * 获取数据表结构
     *
     * @return 数据表结构
     */
    override fun tableVar(): Table<*, *> {
        return tableVar
    }
}