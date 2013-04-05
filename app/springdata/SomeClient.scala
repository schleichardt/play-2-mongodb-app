package springdata

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.List
import beans.BeanProperty

@Component
class SomeClient {
  def findPersons = repository.findByName("Matthews")

  @Autowired
  @BeanProperty
  var repository: PersonRepository = null
}

