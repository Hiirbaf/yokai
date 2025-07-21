package eu.kanade.tachiyomi.ui.setting.controllers

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import eu.kanade.tachiyomi.R
import yokai.i18n.MR

class DiscordConnectionPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : Preference(context, attrs) {

    var onLoginClick: (() -> Unit)? = null
    var onSettingsClick: (() -> Unit)? = null

    init {
        layoutResource = R.layout.preference_discord_connection
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val titleView = holder.findViewById(R.id.discord_title) as? TextView
        val loginButton = holder.findViewById(R.id.discord_login) as? Button
        val settingsButton = holder.findViewById(R.id.discord_settings) as? Button

        titleView?.text = title

        loginButton?.setOnClickListener { onLoginClick?.invoke() }
        settingsButton?.setOnClickListener { onSettingsClick?.invoke() }
    }
}
