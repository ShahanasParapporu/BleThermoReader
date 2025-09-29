package com.takniatech.contec.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.takniatech.contec.data.model.HistoryResultData
import com.takniatech.contec.data.model.TemperatureReading
import com.takniatech.contec.data.model.User

class ContecSQLiteHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "contec.db"
        private const val DATABASE_VERSION = 1

        // User table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_DOB = "dateOfBirth"
        private const val COLUMN_GENDER = "gender"
        private const val COLUMN_WEIGHT = "weight"
        private const val COLUMN_HEIGHT = "height"
        private const val COLUMN_PROFILE_IMAGE_URI = "profileImageUri"

        // Temperature table
        private const val TABLE_TEMPERATURE = "temperature_readings"
        private const val COLUMN_TEMP_ID = "id"
        private const val COLUMN_USER_ID_FK = "userId"
        private const val COLUMN_DEVICE_ADDRESS = "deviceAddress" // NEW
        private const val COLUMN_DEVICE_NAME = "deviceName"       // NEW
        private const val COLUMN_TEMP = "temperature"
        private const val COLUMN_UNIT = "unit"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_IS_REALTIME = "isRealtime"       // NEW (Using INTEGER 0/1)
        private const val COLUMN_DEVICE_ERROR = "deviceError"     // NEW
        private const val COLUMN_RAW_STATE = "rawRetValState"     // NEW
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_DOB TEXT,
                $COLUMN_GENDER TEXT,
                $COLUMN_WEIGHT REAL,
                $COLUMN_HEIGHT REAL,
                $COLUMN_PROFILE_IMAGE_URI TEXT 
            );
        """.trimIndent()

        val createTemperatureTable = """
        CREATE TABLE $TABLE_TEMPERATURE (
            $COLUMN_TEMP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USER_ID_FK INTEGER NOT NULL,
            $COLUMN_DEVICE_ADDRESS TEXT NOT NULL,  -- NEW
            $COLUMN_DEVICE_NAME TEXT,             -- NEW
            $COLUMN_TEMP REAL,
            $COLUMN_UNIT TEXT,
            $COLUMN_TIMESTAMP INTEGER,
            $COLUMN_IS_REALTIME INTEGER,           -- NEW (0 or 1)
            $COLUMN_DEVICE_ERROR TEXT,            -- NEW
            $COLUMN_RAW_STATE INTEGER,            -- NEW
            FOREIGN KEY($COLUMN_USER_ID_FK) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
        );
    """.trimIndent()

        db?.execSQL(createUsersTable)
        db?.execSQL(createTemperatureTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TEMPERATURE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // -------- User CRUD --------
    fun insertUserSync(user: User): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_NAME, user.name)
            put(COLUMN_DOB, user.dateOfBirth)
            put(COLUMN_GENDER, user.gender)
            put(COLUMN_WEIGHT, user.weight)
            put(COLUMN_HEIGHT, user.height)
            put(COLUMN_PROFILE_IMAGE_URI, user.profileImageUri)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun getUserByEmailAndPasswordSync(email: String, password: String): User? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_EMAIL=? AND $COLUMN_PASSWORD=?",
            arrayOf(email, password),
            null,
            null,
            null,
            "1"
        )
        return cursor.use {
            if (it.moveToFirst()) cursorToUser(it) else null
        }
    }

    fun getUserByIdSync(userId: Int): User? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_USER_ID=?",
            arrayOf(userId.toString()),
            null,
            null,
            null,
            "1"
        )
        return cursor.use {
            if (it.moveToFirst()) cursorToUser(it) else null
        }
    }

    fun updateUserSync(user: User): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.password)
            put(COLUMN_NAME, user.name)
            put(COLUMN_DOB, user.dateOfBirth)
            put(COLUMN_GENDER, user.gender)
            put(COLUMN_WEIGHT, user.weight)
            put(COLUMN_HEIGHT, user.height)
            put(COLUMN_PROFILE_IMAGE_URI, user.profileImageUri)
        }
        return db.update(TABLE_USERS, values, "$COLUMN_USER_ID=?", arrayOf(user.id.toString()))
    }

    private fun cursorToUser(cursor: Cursor): User {
        return User(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
            password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
            dateOfBirth = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOB)),
            gender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
            weight = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT)),
            height = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_HEIGHT)),
            profileImageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_IMAGE_URI))
        )
    }

    fun getUserByEmailSync(email: String): User? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_USERS,
            null,
            "$COLUMN_EMAIL=?",
            arrayOf(email),
            null,
            null,
            null,
            "1"
        )
        return cursor.use {
            if (it.moveToFirst()) cursorToUser(it) else null
        }
    }

    fun getLoggedInUserIdSync(): Int? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            null,
            null,
            null,
            null,
            "$COLUMN_USER_ID DESC",
            "1"
        )
        return cursor.use {
            if (it.moveToFirst()) it.getInt(it.getColumnIndexOrThrow(COLUMN_USER_ID))
            else null
        }
    }

    // -------- Temperature CRUD --------
    fun insertTemperatureReadingSync(
        userId: Int,
        deviceAddress: String,
        deviceName: String?,
        temp: Float,
        unit: String,
        timestamp: Long,
        isRealtime: Boolean,
        deviceError: String? = null,
        rawState: Int? = null
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_ID_FK, userId)
            put(COLUMN_DEVICE_ADDRESS, deviceAddress)
            put(COLUMN_DEVICE_NAME, deviceName)
            put(COLUMN_TEMP, temp)
            put(COLUMN_UNIT, unit)
            put(COLUMN_TIMESTAMP, timestamp)
            put(COLUMN_IS_REALTIME, if (isRealtime) 1 else 0)
            put(COLUMN_DEVICE_ERROR, deviceError)
            put(COLUMN_RAW_STATE, rawState)
        }
        return db.insert(TABLE_TEMPERATURE, null, values)
    }

    fun insertTemperatureReadingSync(reading: TemperatureReading): Long {
        return insertTemperatureReadingSync(
            userId = reading.userId,
            deviceAddress = reading.deviceAddress,
            deviceName = reading.deviceName,
            temp = reading.temp,
            unit = reading.unit,
            timestamp = reading.timestamp,
            isRealtime = reading.isRealtime,
            deviceError = reading.deviceError,
            rawState = reading.rawState
        )
    }

    // Batch insert history (avoid duplicates by timestamp + deviceAddress)
    fun insertHistoryBatch(userId: Int, deviceAddress: String, deviceName: String?, history: List<HistoryResultData>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            history.forEach { h ->
                val timestamp = toTimestamp(h.year, h.month, h.day, h.hour, h.min, h.sec)
                // If a row with same timestamp and device exists, skip (simple duplicate prevention)
                val existing = db.rawQuery(
                    "SELECT 1 FROM $TABLE_TEMPERATURE WHERE $COLUMN_TIMESTAMP=? AND $COLUMN_DEVICE_ADDRESS=?",
                    arrayOf(timestamp.toString(), deviceAddress)
                )
                val exists = existing.use { it.moveToFirst() }
                if (!exists) {
                    val cv = ContentValues().apply {
                        put(COLUMN_USER_ID_FK, userId)
                        put(COLUMN_DEVICE_ADDRESS, deviceAddress)
                        put(COLUMN_DEVICE_NAME, deviceName)
                        put(COLUMN_TEMP, h.temp.toFloatOrNull() ?: 0f)
                        put(COLUMN_UNIT, if (h.tempUnit == 0) "C" else "F")
                        put(COLUMN_TIMESTAMP, timestamp)
                        put(COLUMN_IS_REALTIME, 0)
                        put(COLUMN_DEVICE_ERROR, null as String?)
                        put(COLUMN_RAW_STATE, null as String?)
                    }
                    db.insert(TABLE_TEMPERATURE, null, cv)
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // Convert history date to epoch ms (UTC)
    private fun toTimestamp(year: Int, month: Int, day: Int, hour: Int, min: Int, sec: Int): Long {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            // SDK months are likely 1..12; Calendar months are 0..11
            set(java.util.Calendar.MONTH, month - 1)
            set(java.util.Calendar.DAY_OF_MONTH, day)
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, min)
            set(java.util.Calendar.SECOND, sec)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    // Get last realtime reading for a specific device (to compare duplicates)
    fun getLastRealtimeForDevice(userId: Int, deviceAddress: String): TemperatureReading? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TEMPERATURE,
            null,
            "$COLUMN_USER_ID_FK=? AND $COLUMN_DEVICE_ADDRESS=? AND $COLUMN_IS_REALTIME=1",
            arrayOf(userId.toString(), deviceAddress),
            null, null,
            "$COLUMN_TIMESTAMP DESC",
            "1"
        )
        cursor.use {
            if (it.moveToFirst()) {
                return TemperatureReading(
                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_TEMP_ID)),
                    userId = it.getInt(it.getColumnIndexOrThrow(COLUMN_USER_ID_FK)),
                    deviceAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_DEVICE_ADDRESS)),
                    deviceName = it.getString(it.getColumnIndexOrThrow(COLUMN_DEVICE_NAME)),
                    temp = it.getFloat(it.getColumnIndexOrThrow(COLUMN_TEMP)),
                    unit = it.getString(it.getColumnIndexOrThrow(COLUMN_UNIT)),
                    timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                    isRealtime = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_REALTIME)) == 1,
                    deviceError = it.getString(it.getColumnIndexOrThrow(COLUMN_DEVICE_ERROR)),
                    rawState = it.getInt(it.getColumnIndexOrThrow(COLUMN_RAW_STATE))
                )
            }
        }
        return null
    }

    fun getTemperatureReadingsForUserSync(userId: Int): List<TemperatureReading> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TEMPERATURE,
            null,
            "$COLUMN_USER_ID_FK=?",
            arrayOf(userId.toString()),
            null,
            null,
            "$COLUMN_TIMESTAMP DESC"
        )

        val readings = mutableListOf<TemperatureReading>()
        cursor.use {
            while (it.moveToNext()) {
                readings.add(
                    TemperatureReading(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_TEMP_ID)),
                        userId = it.getInt(it.getColumnIndexOrThrow(COLUMN_USER_ID_FK)),
                        deviceAddress = it.getString(it.getColumnIndexOrThrow(COLUMN_DEVICE_ADDRESS)),
                        deviceName = it.getString(it.getColumnIndexOrThrow(COLUMN_DEVICE_NAME)),
                        temp = it.getFloat(it.getColumnIndexOrThrow(COLUMN_TEMP)),
                        unit = it.getString(it.getColumnIndexOrThrow(COLUMN_UNIT)),
                        timestamp = it.getLong(it.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                        isRealtime = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_REALTIME)) == 1,
                        deviceError = it.getString(it.getColumnIndexOrThrow(COLUMN_DEVICE_ERROR)),
                        rawState = it.getInt(it.getColumnIndexOrThrow(COLUMN_RAW_STATE))
                    )
                )
            }
        }
        return readings
    }

    // Total count for UI
    fun getTotalReadingsForUserSync(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_TEMPERATURE WHERE $COLUMN_USER_ID_FK=?",
            arrayOf(userId.toString()))
        cursor.use {
            if (it.moveToFirst()) return it.getInt(0)
        }
        return 0
    }

}