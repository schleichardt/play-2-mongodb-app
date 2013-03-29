package test

import org.specs2.mutable.Specification
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}

class StartPageSpec extends Specification {
  "The start page" should {
    "show the teasers of the 10 most recent blog posts" in new WithApplication {
      val result: Result = controllers.Application.index()(FakeRequest())
      val content = contentAsString(result)
      for (i <- 5 to 14) {
        content must contain(s"title $i")
    }
      content must not contain("title 4")
    }
  }
}
