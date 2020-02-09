package eu.kanade.tachiyomi.ui.library

import android.view.View
import com.bumptech.glide.load.engine.DiskCacheStrategy
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.kanade.tachiyomi.data.glide.GlideApp
import eu.kanade.tachiyomi.source.LocalSource
import kotlinx.android.synthetic.main.catalogue_list_item.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.signature.ObjectKey
import eu.davidea.flexibleadapter.items.IFlexible
import eu.kanade.tachiyomi.data.database.models.MangaImpl
import kotlinx.android.synthetic.main.catalogue_grid_item.*
import kotlinx.android.synthetic.main.catalogue_list_item.download_text
import kotlinx.android.synthetic.main.catalogue_list_item.local_text
import kotlinx.android.synthetic.main.catalogue_list_item.thumbnail
import kotlinx.android.synthetic.main.catalogue_list_item.title
import kotlinx.android.synthetic.main.catalogue_list_item.unread_badge
import kotlinx.android.synthetic.main.catalogue_list_item.unread_text

/**
 * Class used to hold the displayed data of a manga in the library, like the cover or the title.
 * All the elements from the layout file "item_library_list" are available in this class.
 *
 * @param view the inflated view for this holder.
 * @param adapter the adapter handling this holder.
 * @param listener a listener to react to single tap and long tap events.
 * @constructor creates a new library holder.
 */

class LibraryListHolder(
        private val view: View,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>
) : LibraryHolder(view, adapter) {

    /**
     * Method called from [LibraryCategoryAdapter.onBindViewHolder]. It updates the data for this
     * holder with the given manga.
     *
     * @param item the manga item to bind.
     */
    override fun onSetValues(item: LibraryItem) {
        // Update the title of the manga.
        title.text = item.manga.currentTitle()

        // Update the unread count and its visibility.
        with(unread_text) {
            visibility = if (item.manga.unread > 0 && item.unreadType == 1) View.VISIBLE else
                View.GONE
            text = item.manga.unread.toString()
        }
        unread_badge.visibility =
            if (item.manga.unread > 0 && item.unreadType == 0) View.VISIBLE else View.GONE
        // Update the download count and its visibility.
        with(download_text) {
            visibility = if (item.downloadCount > 0) View.VISIBLE else View.GONE
            text = "${item.downloadCount}"
        }
        //show local text badge if local manga
        local_text.visibility = if (item.manga.source == LocalSource.ID) View.VISIBLE else View.GONE

        // Create thumbnail onclick to simulate long click
        thumbnail.setOnClickListener {
            // Simulate long click on this view to enter selection mode
            onLongClick(itemView)
        }

        // Update the cover.
        GlideApp.with(itemView.context).clear(thumbnail)
        GlideApp.with(itemView.context)
                .load(item.manga)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(ObjectKey(MangaImpl.getLastCoverFetch(item.manga.id!!).toString()))
                .centerCrop()
                .circleCrop()
                .dontAnimate()
                .into(thumbnail)
    }

}
