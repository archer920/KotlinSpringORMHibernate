package com.stonesoupprogramming.hibernate

import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.orm.hibernate4.LocalSessionFactoryBean
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.sql.DataSource
import javax.transaction.Transactional

@SpringBootApplication
class SpringJdbcApplication

@Entity
data class User(@get: Id var id: Number = 0,
                @get: Column(name="first_name") var firstName: String = "",
                @get: Column(name = "last_name") var lastName: String = "",
                @get: Column(name = "email") var email: String = "",
                @get: Column(name = "phone") var phone: String = "")


@Configuration
class Configuration {

    @Bean(name = arrayOf("dataSource"))
    fun dataSource(): DataSource = EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("schema.sql")
            .build()


    @Bean
    fun sessionFactory(@Qualifier("dataSource") dataSource: DataSource): LocalSessionFactoryBean {
        val properties = Properties()
        properties.setProperty("dialect", "org.hibernate.dialect.HSQLDB")

        val sessionFactory = LocalSessionFactoryBean().apply {
            setDataSource(dataSource)
            setPackagesToScan(*arrayOf("com.stonesoupprogramming.hibernate"))

        }

        sessionFactory.hibernateProperties = properties
        return sessionFactory
    }

    //Translate Hibernate Exceptions into Spring One
    @Bean
    fun persistenceTranslation() : BeanPostProcessor = PersistenceExceptionTranslationPostProcessor()
}

@Controller
@RequestMapping("/")
class IndexController(@Autowired var indexService: IndexService) {

    @RequestMapping(method = arrayOf(RequestMethod.GET))
    fun doGet(model: Model): String {
        model.apply {
            addAttribute("user", User())
            addAttribute("allUsers", indexService.allUsers())
        }
        return "index"
    }

    @RequestMapping(method = arrayOf(RequestMethod.POST))
    fun doPost(model: Model, user: User): String {
        indexService.addUser(user)
        model.apply {
            addAttribute("user", User())
            addAttribute("allUsers", indexService.allUsers())
        }
        return "index"
    }
}

@Service
@Transactional
class IndexService(@Autowired var indexRepository: IndexRepository) {

    fun addUser(user: User) = indexRepository.addUser(user)

    fun allUsers(): List<User> = indexRepository.allUsers()
}

@Repository
class IndexRepository(@Autowired var sessionFactory: SessionFactory) {

    fun getCurrentSession() = sessionFactory.currentSession!!

    fun addUser(user: User) = getCurrentSession().save(user)!!

    @Suppress("UNCHECKED_CAST")
    fun allUsers(): List<User> = getCurrentSession().createCriteria(User::class.java).list().toList() as List<User>
}

fun main(args: Array<String>) {
    SpringApplication.run(SpringJdbcApplication::class.java, *args)
}
