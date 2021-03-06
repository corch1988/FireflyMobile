package xyz.hisname.fireflyiii

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import xyz.hisname.languagepack.LanguageChanger


class CustomApp: Application() {


    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        LanguageChanger.init(this)
    }

}