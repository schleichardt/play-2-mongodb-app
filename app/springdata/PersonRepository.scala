package springdata

import org.springframework.data.repository.CrudRepository
import java.lang.Long
import java.util.List

abstract trait PersonRepository extends CrudRepository[Person, Long] {
  def findByName(name: String): List[Person]
}
