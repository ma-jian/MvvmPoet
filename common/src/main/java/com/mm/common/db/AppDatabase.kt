package com.mm.common.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mm.common.DefaultSDKInitialize
import com.mm.common.db.dao.LoginUserDao
import com.mm.common.db.dao.UserHostUrlDao
import com.mm.common.db.entity.HostUrl
import com.mm.common.db.entity.LoginDbUser

/**
 * Created by : m
 * @since 1.0
 */

@Database(
    entities = [LoginDbUser::class, HostUrl::class],
    version = 1,
    exportSchema = true,
//    autoMigrations = [AutoMigration(from = 2, to = 3)]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun loginUserDao(): LoginUserDao

    abstract fun settingUrlDao(): UserHostUrlDao

    companion object {
        private const val DATABASE_NAME = "database_room.db"

        // For Singleton instantiation
        @Volatile
        private var instance: AppDatabase? = null

        @JvmStatic
        fun getInstance(): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase().also { instance = it }
            }
        }

        @JvmStatic
        fun closeDb() {
            getInstance().close()
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(): AppDatabase {
            return Room.databaseBuilder(
                DefaultSDKInitialize.mApplication,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            //执行后台任务
//                            val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>()
//                                .setInputData(workDataOf(KEY_FILENAME to PLANT_DATA_FILENAME))
//                                .build()
//                            WorkManager.getInstance(context).enqueue(request)
                        }
                    }
                )
                .allowMainThreadQueries()
//                .addMigrations(migration1_3)
                .fallbackToDestructiveMigration()
                .build()
        }

        private val migration1_3 = object : Migration(1, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `wgt_file` (`id` TEXT PRIMARY KEY NOT NULL," +
                            " `name` TEXT, `contentType` TEXT, `size` TEXT, `uploadDate` TEXT, `md5` TEXT, `content` TEXT, " +
                            "`path` TEXT, `version` TEXT, `orgId` TEXT, `localVersion` TEXT DEFAULT '0') "
                )
            }
        }
    }
}