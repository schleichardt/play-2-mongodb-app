import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import org.specs2.execute.Pending
import models.{PostDAO, MockPostDAO}
import play.api.mvc.{Controller, AnyContent, Action, Result}

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends Specification {
  "Application" should {
    "enable a user to registrate" in {
      Pending
    }

    "enable a user to login" in {
      Pending
    }

    "enable a user to logout" in {
      Pending
    }
  }
}

object TestUtil {
  object ApplicationController extends Controller with controllers.Application {
      val postDAO = MockPostDAO
    }
}

import TestUtil._

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

    "makes older posts paginated available" in {
      Pending
    }
  }
}

class TagPageSpec extends Specification {
  "The tag page" should {
    "show posts according a tag" in {
      Pending
    }
  }
}

class PostPageSpec extends Specification {
  "The post page" should {
    "show a full post with it's comments" in {
      Pending
    }

    "enable a user to make a comment" in {
      Pending
    }
  }
}

class NewPostPageSpec extends Specification {
  "The new post page" should {
    "enable a user to create a new post" in {
      Pending
    }
  }
}


