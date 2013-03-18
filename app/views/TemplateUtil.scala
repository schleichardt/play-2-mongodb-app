package views

import views.html.helper.FieldConstructor

object TemplateUtil {
  implicit def defaultFieldConstructor = FieldConstructor(views.html.form.fieldConstructor.f)
}