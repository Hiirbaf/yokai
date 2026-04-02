package eu.kanade.tachiyomi.util.discord

import android.content.Context
import com.my.kizzyrpc.KizzyRPC
import com.my.kizzyrpc.model.Activity
import com.my.kizzyrpc.model.Assets
import com.my.kizzyrpc.model.Metadata
import com.my.kizzyrpc.model.Timestamps

/**
 * Capa de adaptación entre DiscordRpcManager y la librería KizzyRPC.
 *
 * Referencia: https://github.com/dead8309/KizzyRPC
 *
 * Si en el futuro se cambia la librería subyacente, solo hay que
 * modificar esta clase.
 */
class KizzyRpc(
    private val appId: String,
    private val context: Context,
) {
    private var rpc: KizzyRPC? = null

    suspend fun connect() {
        rpc = KizzyRPC(appId, context)
    }

    suspend fun setActivity(
        name: String,
        details: String,
        state: String,
        largeImage: String?,
        largeText: String?,
        smallImage: String?,
        smallText: String?,
        startTimestamp: Long?,
        buttons: List<Pair<String, String>> = emptyList(),
    ) {
        val kizzy = rpc ?: return

        val assets = Assets(
            largeImage = largeImage,
            largeText = largeText,
            smallImage = smallImage,
            smallText = smallText,
        )

        val timestamps = if (startTimestamp != null) {
            Timestamps(start = startTimestamp)
        } else {
            null
        }

        val metadata = if (buttons.isNotEmpty()) {
            Metadata(buttonUrls = buttons.map { it.second })
        } else {
            null
        }

        val activity = Activity(
            applicationId = appId,
            name = name,
            details = details,
            state = state,
            type = 0, // 0 = Playing
            assets = assets,
            timestamps = timestamps,
            buttons = if (buttons.isNotEmpty()) buttons.map { it.first } else null,
            metadata = metadata,
        )

        kizzy.setActivity(activity, status = "online")
    }

    fun closeRPC() {
        rpc?.closeRPC()
        rpc = null
    }
}
