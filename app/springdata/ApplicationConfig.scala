package springdata

import com.mongodb.Mongo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import play.Play
import java.net.UnknownHostException

@Configuration
@EnableMongoRepositories
class ApplicationConfig {
  @Bean def mongo: Mongo = {
    return new Mongo("localhost")
  }

  @Bean def mongoTemplate: MongoOperations = {
    val m: Mongo = new Mongo
    val operations: MongoOperations = new MongoTemplate(new SimpleMongoDbFactory(m, Play.application.configuration.getString("mongodb.db")))
    if (!operations.collectionExists("persons")) {
      operations.createCollection("persons")
    }
    return operations
  }
}

