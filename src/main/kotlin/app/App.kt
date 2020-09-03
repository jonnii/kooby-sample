package app

import graphql.schema.DataFetcher
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring.newTypeWiring
import io.jooby.ExecutionMode
import io.jooby.Kooby
import io.jooby.OpenAPIModule
import io.jooby.graphql.GraphQLModule
import io.jooby.graphql.GraphQLPlaygroundModule
import io.jooby.hikari.HikariModule
import io.jooby.jdbi.JdbiModule
import io.jooby.json.GsonModule
import io.jooby.pac4j.Pac4jModule
import io.jooby.runApp
import org.pac4j.core.profile.UserProfile
import org.pac4j.http.client.direct.HeaderClient
import org.pac4j.http.client.direct.ParameterClient
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator


data class Foo(
        val id: Int
)

data class Book(
        val id: String,
        val name: String,
        val pageCount: Int
)

data class Author(
        val id: String,
        val firstName: String,
        val lastName: String
)


fun getBookByIdDataFetcher(): DataFetcher<Book> {
    return DataFetcher { Book("hello", "name", 3) }
}

fun getAuthorDataFetcher(): DataFetcher<Author> {
    return DataFetcher { Author("hello", "name", "bob") }
}

class App : Kooby({
    install(OpenAPIModule())
    install(GsonModule())
    install(HikariModule())
    install(JdbiModule())

    install(GraphQLModule(
            RuntimeWiring.newRuntimeWiring()
                    .type(newTypeWiring("Query")
                            .dataFetcher("bookById", getBookByIdDataFetcher()))
                    .type(newTypeWiring("Book")
                            .dataFetcher("author", getAuthorDataFetcher()))
                    .build())
            .setSupportGetRequest(true)
    )
    install(GraphQLPlaygroundModule())

    install(Pac4jModule()
            .client { conf ->
                HeaderClient("Bearer",
                        JwtAuthenticator(SecretSignatureConfiguration(conf.getString("jwt.salt"))))
            }
    )

    get("/") {
        "Welcome to Jooby!"
    }

    get("/foo/{id}") { ctx ->
        val user = ctx.getUser<UserProfile>()

        Foo(3)
    }

})

fun main(args: Array<String>) {
    runApp(args, ExecutionMode.EVENT_LOOP, App::class)
}
