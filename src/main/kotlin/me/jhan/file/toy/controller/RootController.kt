package me.jhan.file.toy.controller

import io.swagger.annotations.ApiOperation
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.result.view.RedirectView
import org.springframework.web.reactive.result.view.View

@Controller
class RootController {

    @GetMapping("/")
    @ApiOperation(value = "swagger 메인 페이지로 리다이렉트 합니다.")
    fun index(): View {
        return RedirectView("/swagger-ui.html");
    }
}