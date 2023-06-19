package board.board.service;

import java.util.List;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import board.board.entity.BoardEntity;
import board.board.entity.BoardFileEntity;

public interface JpaBoardService {
	
	List<BoardEntity> selectBoardList() throws Exception;
	
	void saveBoard(BoardEntity board, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception;
	
	void updateBoard(BoardEntity board, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception;
	
	BoardEntity selectBoardDetail(int boardIdx, String checkResult) throws Exception;
		
	void deleteBoard(int boardIdx) throws Exception;
	
	BoardFileEntity selectBoardFileInformation(int boardIdx, int idx) throws Exception;

}
