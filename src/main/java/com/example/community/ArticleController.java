package com.example.community;

import com.example.community.dto.ArticleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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
            @RequestParam(value = "board", defaultValue = "0")
            Long boardId,
            Model model
    ) {
        ArticleDto articleDto = articleService.readArticle(id);
        model.addAttribute("article", articleDto);
        model.addAttribute("board", boardId);
        model.addAttribute("before", articleService.getFront(boardId, id));
        model.addAttribute("after", articleService.getBack(boardId, id));
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

    @GetMapping("article/hashtag")
    public String byTag(
            @RequestParam("tag")
            String tag,
            Model model
    ) {
        model.addAttribute("tag", tag);
        model.addAttribute("articles", articleService.byTag(tag));
        return "tag";
    }

    @GetMapping("article/search")
    public String search(
            @RequestParam("q")
            String query,
            @RequestParam(value = "board-id", defaultValue = "0")
            Long boardId,
            @RequestParam(value = "criteria")
            String criteria,
            Model model
    ) {
        model.addAttribute("query", query);
        model.addAttribute("boardId", boardId);
        if (!boardId.equals(0L))
            model.addAttribute("boardName", boardService.read(boardId).getName());
        model.addAttribute("criteria", criteria.equals("title") ? "제목" : "내용");
        model.addAttribute("articles", articleService.search(boardId, criteria, query));
        return "search";
    }

    @PostMapping(
            value = "article/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String addImage(
            @PathVariable("id")
            Long id,
            @RequestParam(value = "board", required = false)
            Long board,
            @RequestParam("password")
            String password,
            @RequestParam("image")
            MultipartFile image
    ) {
        articleService.addImage(id, image, password);
        if (board != null)
            return String.format("redirect:/article/%d?board=%d", id, board);
        return String.format("redirect:/article/%d", id);
    }

    @PostMapping(
            "article/{id}/image/{imageId}/delete"
    )
    public String deleteImage(
            @PathVariable("id")
            Long id,
            @PathVariable("imageId")
            Long imageId,
            @RequestParam("password")
            String password,
            @RequestParam(value = "board", required = false)
            Long board
    ) {
        articleService.deleteImage(id, imageId, password);
        if (board != null)
            return String.format("redirect:/article/%d?board=%d", id, board);
        return String.format("redirect:/article/%d", id);
    }


}
