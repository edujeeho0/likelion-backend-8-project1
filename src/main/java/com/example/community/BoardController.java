package com.example.community;

import com.example.community.dto.ArticleDto;
import com.example.community.dto.BoardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("board")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final ArticleService articleService;

    @GetMapping
    public String listAllBoards(
            Model model
    ) {
        model.addAttribute("boards", boardService.readAll());
        model.addAttribute("selected", null);
        List<ArticleDto> articles = articleService.readAll();
        Collections.reverse(articles);
        model.addAttribute("articles", articles);
        return "board";
    }

    @GetMapping("{boardId}")
    public String listOneBoard(
            @PathVariable("boardId")
            Long boardId,
            Model model
    ) {
        BoardDto boardDto = boardService.read(boardId);
        model.addAttribute("boards", boardService.readAll());
        model.addAttribute("selected", boardDto);
        List<ArticleDto> articles = boardDto.getArticles();
        Collections.reverse(articles);
        model.addAttribute("articles", articles);
        return "board";
    }
}
