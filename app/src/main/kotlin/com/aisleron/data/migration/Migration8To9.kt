/*
 * Copyright (C) 2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aisleron.data.base.SyncEntity

class Migration8To9 : Migration(8, 9) {

    private fun setSyncId(
        db: SupportSQLiteDatabase, selectSql: String, updateSql: String
    ) {
        db.query(selectSql).use { cursor ->
            db.compileStatement(updateSql).use { statement ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val syncId = SyncEntity.generateSyncId()

                    statement.bindString(1, syncId)
                    statement.bindLong(2, id)
                    statement.executeUpdateDelete()
                    statement.clearBindings()
                }
            }
        }
    }

    private fun setMissingSyncIds(db: SupportSQLiteDatabase) {
        setSyncId(
            db,
            "SELECT id FROM Note WHERE syncId IS NULL OR syncId = ''",
            "UPDATE Note SET syncId = ? WHERE id = ?"
        )

        setSyncId(
            db,
            "SELECT id FROM Product WHERE syncId IS NULL OR syncId = ''",
            "UPDATE Product SET syncId = ? WHERE id = ?"
        )

        setSyncId(
            db,
            "SELECT id FROM Location WHERE syncId IS NULL OR syncId = ''",
            "UPDATE Location SET syncId = ? WHERE id = ?"
        )

        setSyncId(
            db,
            "SELECT id FROM Aisle WHERE syncId IS NULL OR syncId = ''",
            "UPDATE Aisle SET syncId = ? WHERE id = ?"
        )

        setSyncId(
            db,
            "SELECT id FROM AisleProduct WHERE syncId IS NULL OR syncId = ''",
            "UPDATE AisleProduct SET syncId = ? WHERE id = ?"
        )

        setSyncId(
            db,
            "SELECT id FROM LoyaltyCard WHERE syncId IS NULL OR syncId = ''",
            "UPDATE LoyaltyCard SET syncId = ? WHERE id = ?"
        )

        setSyncId(
            db,
            "SELECT locationId FROM LocationLoyaltyCard WHERE syncId IS NULL OR syncId = ''",
            "UPDATE LocationLoyaltyCard SET syncId = ? WHERE locationId = ?"
        )

        setSyncId(
            db,
            "SELECT id FROM ProductVariant WHERE syncId IS NULL OR syncId = ''",
            "UPDATE ProductVariant SET syncId = ? WHERE id = ?"
        )
    }

    private fun cleanAisleProductOrphans(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            DELETE FROM AisleProduct
            WHERE NOT EXISTS (SELECT 1 FROM Aisle a WHERE a.id = AisleProduct.aisleId)
              OR NOT EXISTS (SELECT 1 FROM Product p WHERE p.id = AisleProduct.productId)
            """.trimIndent()
        )

    }

    private fun recreateAisleProductTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `AisleProduct_new` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                    `aisleId` INTEGER NOT NULL, 
                    `productId` INTEGER NOT NULL, 
                    `rank` INTEGER NOT NULL, 
                    `syncId` TEXT  NOT NULL, 
                    `isRemoved` INTEGER NOT NULL DEFAULT 0, 
                    `lastModifiedAt` INTEGER NOT NULL DEFAULT 0, 
                    `serverUpdatedAt` INTEGER, 
                    FOREIGN KEY(`aisleId`) REFERENCES `Aisle`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                    FOREIGN KEY(`productId`) REFERENCES `Product`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                )
            """.trimIndent()
        )

        db.execSQL(
            """
                INSERT INTO `AisleProduct_new` (
                    `id`, `aisleId`, `productId`, `rank`,`syncId`, 
                    `isRemoved`, `lastModifiedAt`, `serverUpdatedAt`
                ) SELECT 
                    `id`, `aisleId`, `productId`, `rank`,`syncId`, 
                    `isRemoved`, `lastModifiedAt`, `serverUpdatedAt`
                FROM `AisleProduct`
            """.trimIndent()
        )

        db.execSQL("DROP TABLE `AisleProduct`")
        db.execSQL("ALTER TABLE `AisleProduct_new` RENAME TO `AisleProduct`")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_AisleProduct_aisleId_productId` ON `AisleProduct` (`aisleId`, `productId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_AisleProduct_syncId` ON `AisleProduct` (`syncId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_AisleProduct_productId` ON `AisleProduct` (`productId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_AisleProduct_isRemoved_id` ON `AisleProduct` (`isRemoved`, `id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_AisleProduct_lastModifiedAt` ON `AisleProduct` (`lastModifiedAt`)")
    }

    private fun addAisleProductForeignKeys(db: SupportSQLiteDatabase) {
        cleanAisleProductOrphans(db)
        recreateAisleProductTable(db)
    }

    override fun migrate(db: SupportSQLiteDatabase) {
        setMissingSyncIds(db)
        addAisleProductForeignKeys(db)
    }
}