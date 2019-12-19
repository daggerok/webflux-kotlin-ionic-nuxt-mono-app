package com.example.webflux

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.ui.Model
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.RenderingResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toMono
import java.net.URI
import java.time.Duration

@Configuration
class CorsWebFlux : WebFluxConfigurer {

  override fun addCorsMappings(registry: CorsRegistry) {
    registry.addMapping("/**")
            .allowedOrigins("*") // any host or put domain(s) here
            // .allowedMethods("GET, POST") // put the http verbs you want allow
            // .allowedHeaders("Authorization") // put the http headers you want allow
  }

  @Bean
  fun infiniteStream() = Flux.interval(Duration.ofSeconds(1))
      .map { "Hello $it infinite WebFlux stream not from @RestController!" }
      .share()

  @Bean
  fun routes(infiniteStream: Flux<String>) = router {
    resources("/**", ClassPathResource("/static/"))

    "/".nest {
      listOf("/", "/404").forEach {
        GET(it) {
          RenderingResponse.create("index")
              .modelAttribute("message", "Hello not from @Controller!")
              .build()
        }
      }
      "/api".nest {
        GET("/message") {
          ok().contentType(MediaType.APPLICATION_JSON)
              .body(mapOf("message" to "Hello not from @RestController").toMono())
        }
        GET("/messages") {
          ok().contentType(MediaType.TEXT_EVENT_STREAM)
              .body(infiniteStream)
        }
      }
      path("/**") {
        // // RenderingResponse.create("forward:/404").build() // wrong: use redirect instead!
        // // https://jar-download.com/artifacts/org.thymeleaf/thymeleaf-spring5/3.0.10.RELEASE/source-code/org/thymeleaf/spring5/view/reactive/ThymeleafReactiveViewResolver.java
        // RenderingResponse.create("redirect:/404").build()
        //
        // seeOther(URI.create("/404")).build()
        //
        // permanentRedirect(URI.create("/404")).build()
        //
        // ok().render("redirect:/404")
        //
        // ok().header("X-FALLBACK-PATH", it.path())
        //     .header("X-FALLBACK-METHOD", it.methodName())
        //     .render("index", "message" to "WebFlux fallback ${it.path()} from RenderingResponse!")
        //
        val params = it.queryParams()
            .entries.map { e -> e.key to e.value.orEmpty().joinToString(separator = ",") }
            .map { p -> "${p.first}='${p.second}'" }
            .foldRight("") { first, second -> "$first,$second" }
        val model = mapOf(
            "message" to "WebFlux fallback ${it.path()} from RenderingResponse!"
        )
        // ok().header("X-FALLBACK-PATH", it.path())
        //     .header("X-FALLBACK-METHOD", it.methodName())
        //     .header("X-FALLBACK-QUERY-PARAMS", params)
        //     .render("index", model)
        RenderingResponse.create("index")
            .header("X-FALLBACK-PATH", it.path())
            .header("X-FALLBACK-METHOD", it.methodName())
            .header("X-FALLBACK-QUERY-PARAMS", params)
            .modelAttributes(model)
            .build()
      }
    }
  }
}
