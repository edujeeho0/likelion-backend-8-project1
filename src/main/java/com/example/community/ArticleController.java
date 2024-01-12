package com.example.community;

import com.example.community.dto.ArticleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class ArticleController {
    private final BoardService boardService;
    private final ArticleService articleService;

    @GetMapping("article/new")
    public String newArticle(
            Model model
    ) {
        model.addAttribute("boards", boardService.readAll());
        return "article/new";
    }

    @PostMapping("article")
    public String createArticle(
            @RequestParam("title")
            String title,
            @RequestParam("content")
            String content,
            @RequestParam("password")
            String password,
            @RequestParam("board-id")
            Long boardId
    ) {
        Long newId = articleService.create(boardId, new ArticleDto(title, content, password)).getId();
        return String.format("redirect:/article/%d", newId);
    }

    @GetMapping("article/{id}")
    public String readArticle(
            @PathVariable("id")
            Long id,
            Model model
    ) {
        model.addAttribute("article", articleService.readArticle(id));
        return "article/read";
    }

    @GetMapping("article/{id}/edit")
    public String editArticle(
            @PathVariable("id")
            Long id,
            Model model
    ) {
        model.addAttribute("article", articleService.readArticle(id));
        return "article/edit";
    }

    @PostMapping("article/{id}/update")
    public String updateArticle(
            @PathVariable("id")
            Long id,
            @RequestParam("title")
            String title,
            @RequestParam("content")
            String content,
            @RequestParam("password")
            String password
    ) {
        articleService.update(id, new ArticleDto(title, content, password));
        return String.format("redirect:/article/%d", id);
    }


    @PostMapping("article/{id}/delete")
    public String deleteArticle(
            @PathVariable("id")
            Long id,
            @RequestParam("password")
            String password
    ) {
        articleService.delete(id, password);
        return "redirect:/board";
    }

}
