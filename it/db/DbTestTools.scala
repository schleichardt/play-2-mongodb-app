package info.schleichardt.ic2.db

import play.api.Application
import se.radley.plugin.salat.SalatPlugin

object DbTestTools {
  def salatPlugin(implicit app: Application): SalatPlugin = app.plugin(classOf[SalatPlugin]).get
}