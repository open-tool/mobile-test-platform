package com.atiurin.atp.farmserver.db

import org.jetbrains.exposed.sql.Database

open class DataSource(val db: Database? = null)

