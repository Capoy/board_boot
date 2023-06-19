package board.board.service;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import board.board.dto.BoardDto;
import board.board.dto.BoardFileDto;
import board.board.mapper.BoardMapper;
import board.common.FileUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
//@Transactional
public class BoardServiceImpl implements BoardService {
	
	@Autowired
	private BoardMapper boardMapper;
	
	@Autowired
	private FileUtils fileUtils;
	
	@Override
	public List<BoardDto> selectBoardList() throws Exception {
		
		return boardMapper.selectBoardList();
	}
	
	@Override
	public void insertBoard(BoardDto board, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
		
		boardMapper.insertBoard(board);
		
		if(ObjectUtils.isEmpty(multipartHttpServletRequest) == false) {
			
			Iterator<String> iterator = multipartHttpServletRequest.getFileNames();
			
			String name;
			
			while(iterator.hasNext()) {
				
				name = iterator.next();
				
				log.debug("file tag name: " + name);
				
				List<MultipartFile> list = multipartHttpServletRequest.getFiles(name);
				
				for(MultipartFile multipartFile : list) {
					
					log.debug("start file information");
					log.debug("file name : " + multipartFile.getOriginalFilename());
					log.debug("file size : " + multipartFile.getSize());
					log.debug("file content type : " + multipartFile.getContentType());
					log.debug("end file information.\n");
					
				}
				
			}
			
			List<BoardFileDto> list = fileUtils.parseFileInfo(board.getBoardIdx(), multipartHttpServletRequest);
			
			if(CollectionUtils.isEmpty(list) == false) {
				
				boardMapper.insertBoardFileList(list);
			}
			
		}
	}
	
	@Override
	public BoardDto selectBoardDetail(int boardIdx, String checkResult) throws Exception {
		
		if(checkResult.equals("Y")) {
			
			boardMapper.updateHitCount(boardIdx);
		}
		
		BoardDto board = boardMapper.selectBoardDetail(boardIdx);
		
		List<BoardFileDto> fileList = boardMapper.selectBoardFileList(boardIdx);
		
		board.setFileList(fileList);
		
		return board;
	}
	
	@Override
	public void updateBoard(BoardDto board, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
		
		boardMapper.updateBoard(board);
							
		List<BoardFileDto> newFileList = fileUtils.parseFileInfo(board.getBoardIdx(), multipartHttpServletRequest);
		
		if(CollectionUtils.isEmpty(newFileList) == false) {
			
			List<BoardFileDto> oldFileList = boardMapper.selectBoardFileList(board.getBoardIdx());
			
			fileUtils.deleteFiles(oldFileList);
			
			boardMapper.deleteBoardFileList(board.getBoardIdx());
			
			boardMapper.insertBoardFileList(newFileList);
		}

	}
	
	@Override
	public void deleteBoard(int boardIdx) throws Exception {
		
		List<BoardFileDto> fileList = boardMapper.selectBoardFileList(boardIdx);
		
		boardMapper.deleteBoardFileList(boardIdx);
		
		boardMapper.deleteBoard(boardIdx);
		
		fileUtils.deleteFiles(fileList);	
	}	
	
	@Override
	public BoardFileDto selectBoardFileInformation(int idx, int boardIdx) throws Exception {
		
		return boardMapper.selectBoardFileInformation(idx, boardIdx);
	}
}
