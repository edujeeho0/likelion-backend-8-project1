package com.example.community;

import com.example.community.dto.BoardDto;
import com.example.community.entity.Board;
import com.example.community.repo.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private static final String[] basicBoardNames = {
            "자유 게시판",
            "개발 게시판",
            "일상 게시판",
            "사건사고 게시판"
    };

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
        for (String boardName: basicBoardNames) {
            if (!this.boardRepository.existsByName(boardName)) {
                Board board = new Board();
                board.setName(boardName);
                this.boardRepository.save(board);
            }
        }
    }

    public List<BoardDto> readAll() {
        List<BoardDto> boardDtos = new ArrayList<>();
        for (Board board: boardRepository.findAll()) {
            boardDtos.add(BoardDto.fromEntity(board));
        }
        return boardDtos;
    }

    public BoardDto read(Long boardId) {
        return BoardDto.fromEntity(
                boardRepository.findById(boardId).orElseThrow());
    }
}
