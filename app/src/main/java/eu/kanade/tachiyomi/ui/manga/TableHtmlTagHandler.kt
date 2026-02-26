package eu.kanade.tachiyomi.ui.manga

import io.noties.markwon.html.HtmlTagHandler

class TableHtmlTagHandler : HtmlTagHandler {
    override fun handles(tag: String): Boolean {
        return tag.equals("table", ignoreCase = true) || 
               tag.equals("tr", ignoreCase = true) || 
               tag.equals("td", ignoreCase = true)
    }

    override fun handle(tag: String, content: String): String {
        if (tag.equals("table", ignoreCase = true)) {
            return "<table>$content</table>"
        }
        return content
    }
}
