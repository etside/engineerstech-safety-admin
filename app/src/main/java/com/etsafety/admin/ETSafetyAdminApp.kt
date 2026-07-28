package com.etsafety.admin

import android.app.Application
import com.etsafety.admin.data.AppDatabase

class ETSafetyAdminApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
