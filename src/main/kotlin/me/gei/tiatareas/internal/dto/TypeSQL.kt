package me.gei.tiatareas.internal.dto

import taboolib.module.database.*

/**
 * TypeSQL 类表示 SQL 数据库的类型实现。
 *
 * @property host SQL 数据库的主机配置
 * @property table 数据库表名
 */
class TypeSQL(private val host: Host<SQL>, private val table: String) : Type() {

    /**
     * 表示数据库表结构的 Table 对象
     */
    private val tableVar = Table(table, host) {
        add { id() }
        add("user") {
            type(ColumnTypeSQL.VARCHAR) {
                options(ColumnOptionSQL.UNIQUE_KEY)
            }
        }
        add("discovering") {
            type(ColumnTypeSQL.JSON)
        }
        add("discovered") {
            type(ColumnTypeSQL.JSON)
        }
    }

    /**
     * 获取数据库主机配置
     *
     * @return 返回 Host 对象，表示数据库连接的主机配置
     */
    override fun host(): Host<*> {
        return host
    }

    /**
     * 获取数据库表配置
     *
     * @return 返回 Table 对象，表示数据库表的配置
     */
    override fun tableVar(): Table<*, *> {
        return tableVar
    }
}