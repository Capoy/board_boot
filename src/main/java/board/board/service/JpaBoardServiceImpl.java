package board.board.service;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import board.board.dto.BoardFileDto;
import board.board.entity.BoardEntity;
import board.board.entity.BoardFileEntity;
import board.board.repository.JpaBoardRepository;
import board.common.FileUtils;

@Service
public class JpaBoardServiceImpl implements JpaBoardService {
	
	@Autowired
	JpaBoardRepository jpaBoardRepository;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	FileUtils fileUtils;
	
	@Override
	public List<BoardEntity> selectBoardList() throws Exception {
		
		return jpaBoardRepository.findAllByOrderByBoardIdxDesc();
	}

	@Override
	public void saveBoard(BoardEntity board, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
			
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
			
			List<BoardFileEntity> list = fileUtils.parseFileInfo(multipartHttpServletRequest);
			
			if(CollectionUtils.isEmpty(list) == false) {
				
				board.setFileList(list);
			}
			
		}
				
		jpaBoardRepository.save(board);	// @OneToMany 연관관계로 해당 보드의 파일리스트까지 같이
	}
	
	@Override
	public void updateBoard(BoardEntity board, MultipartHttpServletRequest multipartHttpServletRequest) throws Exception {
			
		List<BoardFileEntity> newFileList = fileUtils.parseFileInfo(multipartHttpServletRequest);
		
		if(CollectionUtils.isEmpty(newFileList) == false) {
			
			List<BoardFileEntity> oldFileList = jpaBoardRepository.findBoardFileList(board.getBoardIdx());
			jpaBoardRepository.deleteBoardFileList(board.getBoardIdx());
			
			fileUtils.deleteFilesJpa(oldFileList);
			
			board.setFileList(newFileList);
		}
				
		jpaBoardRepository.save(board);	// @OneToMany 연관관계로 해당 보드의 파일리스트까지 같이
	}

	@Override
	public BoardEntity selectBoardDetail(int boardIdx, String checkResult) throws Exception {
		
		Optional<BoardEntity> optional = jpaBoardRepository.findById(boardIdx);
		
		if(optional.isPresent()) {
			
			BoardEntity board = optional.get();
			
			if(checkResult.equals("Y")) {
				
				board.setHitCnt(board.getHitCnt() + 1);
				
				jpaBoardRepository.save(board);
			}
						
			return board;
			
		} else {
			
			throw new NullPointerException();
		}
		
	}

	@Override
	public void deleteBoard(int boardIdx) throws Exception {
		
		List<BoardFileEntity> list = jpaBoardRepository.findBoardFileList(boardIdx);
		
		jpaBoardRepository.deleteById(boardIdx);
		
		fileUtils.deleteFilesJpa(list);
	}

	@Override
	public BoardFileEntity selectBoardFileInformation(int boardIdx, int idx) throws Exception {
		
		BoardFileEntity boardFile = jpaBoardRepository.findBoardFile(boardIdx, idx);
		
		return boardFile;
	}

}
